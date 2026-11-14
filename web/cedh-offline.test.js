import {test} from 'node:test';
import assert from 'node:assert/strict';
import vm from 'node:vm';
import {readFile} from 'node:fs/promises';
test('shell offline cEDH inclui bootstrap, interface e motor',async()=>{
 const source=await readFile(new URL('sw.js',import.meta.url),'utf8'),listeners={},cached=new Map();
 const context={URL,Response,self:{location:{origin:'https://offline.test'},clients:{claim:async()=>{}},addEventListener:(e,fn)=>listeners[e]=fn},caches:{open:async()=>({addAll:async paths=>{for(const path of paths){assert.ok((await readFile(new URL(path==='./'?'./index.html':path,import.meta.url))).length);cached.set(path,{url:path});}}}),match:async r=>cached.get(typeof r==='string'?r:r.url)},fetch:async()=>{throw Error('offline')}};
 vm.runInNewContext(source,context);let pending;listeners.install({waitUntil:p=>pending=p});await pending;
 for(const path of ['./bootstrap.js','./cedh-engine.js','./cedh-app.js'])assert.ok(cached.has(path));
 let response;listeners.fetch({request:{url:'https://offline.test/?mode=cedh',method:'GET',mode:'navigate'},respondWith:p=>response=p});assert.equal((await response).url,'./index.html');
 const index=await readFile(new URL('index.html',import.meta.url),'utf8');assert.match(index,/bootstrap.js/);
});
