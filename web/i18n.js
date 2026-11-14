import pt from './i18n-pt-BR.js';
import en from './i18n-en-US.js';
import es from './i18n-es-ES.js';

export const catalogs = {'pt-BR':pt,'en-US':en,'es-ES':es};
export const preferenceKey = 'championship-language';
export function detectLanguage(tag='') {
  return ({pt:'pt-BR',en:'en-US',es:'es-ES'})[String(tag).toLowerCase().split(/[-_]/)[0]] || 'pt-BR';
}
export function createI18n({storage,deviceLanguage='pt-BR'}={}) {
  let saved;
  try { saved=storage?.getItem(preferenceKey); } catch {}
  let language=Object.hasOwn(catalogs,saved)?saved:detectLanguage(deviceLanguage);
  const listeners=new Set();
  const api={
    get language(){return language;},
    setLanguage(tag){
      const next=Object.hasOwn(catalogs,tag)?tag:'pt-BR';
      try { storage?.setItem(preferenceKey,next); } catch {}
      if(language!==next){language=next;listeners.forEach(fn=>fn(language));}
    },
    subscribe(fn){listeners.add(fn);return ()=>listeners.delete(fn);},
    t(key,params={}){
      let value=catalogs[language][key]??pt[key];
      if(value==null)return `[${key}]`;
      if(typeof value==='object')value=value[new Intl.PluralRules(language).select(Number(params.count))]??value.other;
      return value.replace(/\{(\w+)\}/g,(match,name)=>Object.hasOwn(params,name)?String(params[name]):match);
    },
    number(value,options={}){return new Intl.NumberFormat(language,options).format(value);},
    percent(value){return api.number(value,{style:'percent',maximumFractionDigits:2});},
    money(value){return api.number(value,{style:'currency',currency:'BRL'});},
    date(value,options={dateStyle:'medium'}){return new Intl.DateTimeFormat(language,options).format(new Date(value));}
  };
  return api;
}
let storage;
try { storage=globalThis.localStorage; } catch {}
export const i18n=createI18n({storage,deviceLanguage:globalThis.navigator?.language});
export const t=(key,params)=>i18n.t(key,params);
