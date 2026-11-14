import {i18n,t} from './i18n.js';
const selector=document.createElement('section');
selector.innerHTML='<label>Modalidade<select id="modality"><option value="duplas">Campeonato de Duplas</option><option value="cedh">cEDH Multiplayer (4 jogadores)</option><option value="dc500">Duel Commander 500 (1?1)</option></select></label>';
document.querySelector('header').append(selector);
const languageLabel=document.createElement('label');
languageLabel.textContent='Idioma / Language / Idioma';
const languageSelect=document.createElement('select');
languageSelect.id='language';
for(const [tag,name] of [['pt-BR','🇧🇷 Português (Brasil)'],['en-US','🇺🇸 English (United States)'],['es-ES','🇪🇸 Español (España)']]) {
  const option=document.createElement('option');option.value=tag;option.textContent=name;languageSelect.append(option);
}
languageLabel.append(languageSelect);selector.prepend(languageLabel);
function translateShell(){
  languageSelect.value=i18n.language;
  document.documentElement.lang=i18n.language;
  const select=selector.querySelector('#modality');
  for(const option of select.options)option.textContent=t(option.value);
  select.parentElement.firstChild.textContent=t('modality');
}
languageSelect.onchange=()=>i18n.setLanguage(languageSelect.value);
i18n.subscribe(translateShell);translateShell();
const selected=document.querySelector('#modality');
const requested=new URL(location.href).searchParams.get('mode');
const mode=['cedh','dc500'].includes(requested)?requested:'duplas';
selected.value=mode;
selected.onchange=()=>{const url=new URL(location.href);url.searchParams.set('mode',selected.value);location.href=url.href;};
await import(mode==='dc500'?'./dc500-app.js':mode==='cedh'?'./cedh-app.js':'./app.js');
