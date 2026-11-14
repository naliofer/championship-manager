import {readFile,writeFile,mkdir} from 'node:fs/promises';
await mkdir('dist',{recursive:true});
for(const file of ['index.html','style.css','engine.js','app.js','bootstrap.js','cedh-engine.js','cedh-app.js','dc500-engine.js','dc500-store.js','dc500-app.js','sw.js','manifest.webmanifest','icon.png']) await writeFile(`dist/${file}`,await readFile(file));
console.log('PWA preparada em web/dist');
