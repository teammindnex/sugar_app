import os
import re

directory = 'src/main/java/com/sugarcane/erp/controller'

for filename in os.listdir(directory):
    if filename.endswith('.java'):
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Find all DatePicker declarations
        date_pickers = re.findall(r'@FXML\s+private\s+DatePicker\s+(\w+);', content)
        
        # Some controllers define DatePickers without @FXML, let's find all DatePicker declarations
        date_pickers.extend(re.findall(r'DatePicker\s+(\w+)\s*=\s*new\s+DatePicker', content))

        if not date_pickers:
            continue

        date_pickers = list(set(date_pickers))

        print(f"Modifying {filename} - Found DatePickers: {date_pickers}")

        # Add setConverter for each datepicker in initialize if not already there
        for dp in date_pickers:
            # Check if setConverter is already there
            if f'{dp}.setConverter' not in content:
                # Find initialize method and inject
                # public void initialize() {
                init_match = re.search(r'public\s+void\s+initialize\s*\(\)\s*\{', content)
                if init_match:
                    insert_pos = init_match.end()
                    injection = f'\n        if ({dp} != null) {dp}.setConverter(com.sugarcane.erp.utils.DateUtil.getDateConverter());'
                    content = content[:insert_pos] + injection + content[insert_pos:]
                else:
                    print(f"Could not find initialize() in {filename}")
            else:
                # Replace the setConverter logic if it uses custom dd/MM/yyyy
                if 'new StringConverter' in content or 'dateConverter' in content:
                     # This gets complicated to parse using regex. 
                     # I will just manually edit the ones that have it, or global replace dd/MM/yyyy to dd-MM-yyyy.
                     pass

        # Replace dd/MM/yyyy with dd-MM-yyyy
        content = content.replace('dd/MM/yyyy', 'dd-MM-yyyy')

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
