import {readFile,writeFile,mkdir} from 'node:fs/promises';
const output=process.env.PWA_BUILD_DIR || 'dist';
await mkdir(output,{recursive:true});
for(const file of ['index.html','style.css','engine.js','app.js','bootstrap.js','cedh-engine.js','cedh-app.js','dc500-engine.js','dc500-store.js','dc500-app.js','sw.js','manifest.webmanifest','icon.png']) await writeFile(`${output}/${file}`,await readFile(file));
console.log(`PWA preparada em ${output}`);
for(const file of ['i18n.js','i18n-pt-BR.js','i18n-en-US.js','i18n-es-ES.js']) await writeFile(`${output}/${file}`,await readFile(file));
