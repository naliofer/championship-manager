import {test} from 'node:test';
import assert from 'node:assert/strict';
import {catalogs,createI18n,detectLanguage} from './i18n.js';
import {readFile} from 'node:fs/promises';

test('Android catalog keys and format parameters match',async()=>{
  const catalogs=await Promise.all(['values','values-en-rUS','values-es-rES'].map(async folder=>{
    const xml=await readFile(new URL(`../app/src/main/res/${folder}/strings.xml`,import.meta.url),'utf8');
    return Array.from(xml.matchAll(/<string\s+name="([^"]+)"([^>]*)>([\s\S]*?)<\/string>/g))
      .filter(match=>!match[2].includes('translatable="false"'))
      .map(match=>[match[1],Array.from(match[3].matchAll(/%\d+\$[dsf]/g),m=>m[0]).sort()]).sort();
  }));
  assert.deepEqual(catalogs[1],catalogs[0]);assert.deepEqual(catalogs[2],catalogs[0]);
});

test('initial language detection and fallback',()=>{
  for(const [tag,expected] of [['pt-PT','pt-BR'],['en-GB','en-US'],['es-MX','es-ES'],['fr-FR','pt-BR'],['','pt-BR']])assert.equal(detectLanguage(tag),expected);
});
test('switch, immediate notification and persisted preference',()=>{
  const data=new Map();const storage={getItem:k=>data.get(k),setItem:(k,v)=>data.set(k,v)};
  const a=createI18n({storage,deviceLanguage:'es'});let events=0;
  const unsubscribe=a.subscribe(()=>events++);
  for(const tag of Object.keys(catalogs)){a.setLanguage(tag);assert.equal(a.language,tag);assert.equal(createI18n({storage,deviceLanguage:'fr'}).language,tag);}
  assert.equal(events,3);unsubscribe();a.setLanguage('invalid');assert.equal(a.language,'pt-BR');assert.equal(events,3);
});
test('parameters, plurals, missing keys and Portuguese fallback',()=>{
  const a=createI18n({deviceLanguage:'en'});
  assert.equal(a.t('players',{count:1}),'1 player');assert.equal(a.t('players',{count:2}),'2 players');
  assert.equal(a.t('roundOf',{round:2,total:4}),'Round 2 of 4');assert.equal(a.t('unknown'),'[unknown]');
  catalogs['pt-BR'].fallbackTest='Português';assert.equal(a.t('fallbackTest'),'Português');delete catalogs['pt-BR'].fallbackTest;
});
test('catalog keys and plural forms are identical',()=>{
  const shape=c=>Object.entries(c).map(([k,v])=>[k,typeof v==='object'?Object.keys(v).sort():typeof v]).sort();
  for(const catalog of Object.values(catalogs))assert.deepEqual(shape(catalog),shape(catalogs['pt-BR']));
});
test('regional formatting retains BRL and survives inaccessible storage',()=>{
  for(const language of Object.keys(catalogs)){
    const a=createI18n({deviceLanguage:language,storage:{getItem(){throw Error();},setItem(){throw Error();}}});
    assert.equal(a.number(1234.5),new Intl.NumberFormat(language).format(1234.5));
    assert.equal(a.money(500),new Intl.NumberFormat(language,{style:'currency',currency:'BRL'}).format(500));
    assert.equal(a.percent(.5),new Intl.NumberFormat(language,{style:'percent',maximumFractionDigits:2}).format(.5));
    assert.equal(a.date('2026-11-14T12:00:00Z'),new Intl.DateTimeFormat(language,{dateStyle:'medium'}).format(new Date('2026-11-14T12:00:00Z')));
    a.setLanguage('es-ES');
  }
});
