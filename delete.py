import shutil
import os

path = r"\\?\C:\Users\sif-\Desktop\All projects\SugarCane Supplier\target"
if os.path.exists(path):
    print("Deleting long path...")
    shutil.rmtree(path)
    print("Deleted.")
else:
    print("Path not found.")
