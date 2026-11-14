import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {JSDOM} from 'jsdom';
import {IDBFactory} from 'fake-indexeddb';
import * as E from './cedh-engine.js';
const source=(await readFile(new URL('./cedh-app.js',import.meta.url),'utf8')).replace(/^import .*;\r?\n/,'');
async function waitFor(predicate){for(let i=0;i<200;i++){if(await predicate())return;await new Promise(r=>setTimeout(r,2));}throw Error('Estado esperado não apareceu');}
async function mount(indexedDB){
 const dom=new JSDOM('<header></header><p id="status"></p><nav></nav><main id="app"></main>',{url:'https://cedh.test',runScripts:'outside-only'});
 const w=dom.window,errors=[];w.indexedDB=indexedDB;w.alert=message=>errors.push(message);w.confirm=()=>true;
 await new w.Function('E',`return (async()=>{${source}\n})()`)(E);
 const q=selector=>w.document.querySelector(selector);
 return {dom,w,errors,q,async click(action){const el=q(`[data-action="${action}"]`);assert.ok(el,action);assert.equal(el.disabled,false,action);el.click();await new Promise(r=>setTimeout(r,12));assert.deepEqual(errors,[]);},async fill(selector,value){const el=q(selector);assert.ok(el,selector);el.value=value;el.dispatchEvent(new w.Event('change',{bubbles:true}));await new Promise(r=>setTimeout(r,12));assert.deepEqual(errors,[]);},async tab(i){q(`[data-tab="${i}"]`).click();await new Promise(r=>setTimeout(r,5));}};
}
async function current(factory){const db=await new Promise((resolve,reject)=>{const r=factory.open('camp-d-house',1);r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error);});try{return await new Promise((resolve,reject)=>{const r=db.transaction('data').objectStore('data').get('cedh_current');r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error);});}finally{db.close();}}
for(const count of [8,12])test(`DOM + IndexedDB: criar ${count}, sortear, registrar, reabrir e concluir${count===12?' com Top 4':''}`,async()=>{
 const factory=new IDBFactory();let ui=await mount(factory);
 try{
 await ui.fill('#name','Integração '+count);await ui.fill('#config-rounds','2');
 if(count===12){const el=ui.q('#top4');el.checked=true;el.dispatchEvent(new ui.w.Event('change',{bubbles:true}));await waitFor(async()=>(await current(factory))?.config.top4);}
 for(let i=0;i<count;i++){
  await ui.click('add');const articles=ui.w.document.querySelectorAll('#app .grid article'),article=articles[articles.length-1],id=article.querySelector('[data-player]').dataset.player;
  for(const [field,value]of Object.entries({name:'Jogador '+i,commanders:'Comandante igual',deck:'Deck '+i,moxfield:''}))await ui.fill(`[data-player="${id}"][data-field="${field}"]`,value);
 }
 await ui.tab(1);await ui.click('preview');assert.equal((await current(factory)).tables.length,0);await ui.click('confirm');
 await ui.click('clock:start:1');await waitFor(async()=>(await current(factory)).clocks[1]?.since!==null);
 const first=await current(factory);assert.ok(first.clocks[1].since);ui.dom.window.close();ui=await mount(factory);await ui.tab(1);assert.match(ui.q('#app').textContent,/Jogador 0/);
 await ui.click('clock:pause:1');assert.equal((await current(factory)).clocks[1].since,null);
 async function finishPending(){const s=await current(factory);for(let i=0;i<s.tables.length;i++){if(s.tables[i].result)continue;await ui.click('edit:'+i);const choice=ui.q('#outcome');choice.value='win:'+s.tables[i].seats[0];ui.q('#result').dispatchEvent(new ui.w.Event('submit',{bubbles:true,cancelable:true}));await waitFor(async()=>!!(await current(factory)).tables[i].result);assert.deepEqual(ui.errors,[]);}}
 await finishPending();await ui.tab(2);assert.match(ui.q('#app').textContent,/5 pts/);await ui.tab(1);
 await ui.click('edit:0');await ui.fill('#outcome','TIME_DRAW');ui.q('[data-eliminated]').checked=true;ui.q('#result').dispatchEvent(new ui.w.Event('submit',{bubbles:true,cancelable:true}));await waitFor(async()=>(await current(factory)).tables[0].result.outcome==='TIME_DRAW');
 assert.equal(E.ranking(await current(factory)).reduce((n,r)=>n+r.points,0),count/4*5-1);
 await ui.click('preview');await ui.click('confirm');await finishPending();
 if(count===12){await ui.click('preview');await ui.click('confirm');await finishPending();}
 const completed=await current(factory);assert.ok(E.champion(completed));ui.dom.window.close();ui=await mount(factory);await ui.tab(2);assert.match(ui.q('#app').textContent,/Campeão:/);assert.deepEqual(ui.errors,[]);
 await ui.tab(0);await ui.click('new');assert.equal((await current(factory)).tables.length,0);await ui.click('archives');assert.match(ui.q('#archives').textContent,/Integração/);await ui.click('restore:'+completed.id);assert.equal((await current(factory)).tables.length,completed.tables.length);
 }finally{ui.dom.window.close();}
});
