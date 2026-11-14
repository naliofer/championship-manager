import * as E from './dc500-engine.js';
const KEY='dc500_current_v1';
export function load(storage=localStorage){const raw=storage.getItem(KEY);return raw?E.decode(raw):E.initial();}
export function persist(state,storage=localStorage){const raw=E.encode(state);storage.setItem(KEY,raw);return state;}
export function archive(state,storage=localStorage){storage.setItem('dc500_archive_'+state.id,E.encode(state));}
export function archives(storage=localStorage){return Array.from({length:storage.length},(_,i)=>storage.key(i)).filter(k=>k.startsWith('dc500_archive_')).map(k=>E.decode(storage.getItem(k)));}
