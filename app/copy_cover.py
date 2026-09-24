import shutil
import os

# Source path provided by the system
source_file = "/tmp/c00578a3786ccc24ab28d622c0588e0be38fdde82cd95b74547eac5691542038 (1).png"
# Destination in drawable directory
dest_file = "/app/src/main/res/drawable/book_cover.png"

# Ensure lowercase for drawable resource name
if os.path.exists(source_file):
    shutil.copy(source_file, dest_file)
    print(f"Copied {source_file} to {dest_file}")
else:
    print(f"File {source_file} not found.")
