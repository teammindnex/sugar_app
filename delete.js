const fs = require('fs');
const path = require('path');

const targetPath = path.resolve(__dirname, 'target');
if (fs.existsSync(targetPath)) {
    console.log("Deleting deeply nested target directory using Node...");
    fs.rmSync(targetPath, { recursive: true, force: true });
    console.log("Deleted successfully.");
} else {
    console.log("Not found.");
}
