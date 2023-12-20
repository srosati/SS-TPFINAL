#!/bin/bash


# This program takes a list of config files as arguments and runs the program for each one,
# generating ovito files:
# The output is then saved to ovito_outputs/{file_path}
# Make sure to have directories & subdirectories created before running this script
# Example: ./run_multiple.sh configs/*.txt
# Output: ovito_outputs/configs/{file_name}

# Iterate over each file in the list of arguments
for file in "$@"; do
    # Check if it's a file
    if [[ -f "$file" ]]; then
        echo "Running for file $file"

        # Run the code for the file
        mvn exec:java -Dexec.mainClass="ar.edu.itba.ss.Main" -Dexec.args="$file"

        cd graphics
        python3 ovito.py
        cd ..

        # Remove extension
        file="${file%.*}"

        cp "outFiles/ovito.txt" "ovito_outputs/$file.txt"
        cp "outFiles/walls.txt" "ovito_outputs/$file-walls.txt"
    fi
done