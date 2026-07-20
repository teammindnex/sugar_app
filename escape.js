const fs = require('fs');

const path = 'src/main/resources/i18n/messages_mr.properties';
const content = fs.readFileSync(path, 'utf8');

let escapedContent = '';
for (let i = 0; i < content.length; i++) {
    const char = content[i];
    const code = content.charCodeAt(i);
    if (code > 127) {
        escapedContent += '\\u' + code.toString(16).padStart(4, '0');
    } else {
        escapedContent += char;
    }
}

fs.writeFileSync(path, escapedContent, 'utf8');
console.log('Converted messages_mr.properties to unicode escapes!');
