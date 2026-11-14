const selector=document.createElement('section');
selector.innerHTML='<label>Modalidade<select id="modality"><option value="duplas">Campeonato de Duplas</option><option value="cedh">cEDH Multiplayer (4 jogadores)</option><option value="dc500">Duel Commander 500 (1?1)</option></select></label>';
document.querySelector('header').append(selector);
const selected=document.querySelector('#modality');
const requested=new URL(location.href).searchParams.get('mode');
const mode=['cedh','dc500'].includes(requested)?requested:'duplas';
selected.value=mode;
selected.onchange=()=>{const url=new URL(location.href);url.searchParams.set('mode',selected.value);location.href=url.href;};
await import(mode==='dc500'?'./dc500-app.js':mode==='cedh'?'./cedh-app.js':'./app.js');
