#!/bin/bash


# This program takes to arguments:
# 1. The directory path that contains all the config files to run
# 2. The output file path
# Before each run it appends the variable to the output file
# After each run it runs the python script to aggregate the flow for that run
# Then the user can run the python script to plot the results


# Get the directory path from the first script argument
dir_path="$1"
output_file="$2"

# Check if the directory exists
if [[ ! -d "$dir_path" ]]; then
    echo "Directory $dir_path does not exist."
    exit 1
fi

# Empty the output file
rm "$output_file"
touch "$output_file"

# Iterate over each file in the directory
for file in "$dir_path"/*; do
    # Check if it's a file (not a directory)
    if [[ -f "$file" ]]; then
        # Parse the variable from the file name
        # file is named as: "{dir}/{variable}.txt"
        # variable is a number, if it's a float, it's separated by an underscore
        # Example: "dir/1.txt", "dir/1_5.txt"

        # Get the file name without the directory path
        file_name=$(basename "$file")
        # Get the file name without the extension
        file_name="${file_name%.*}"
        # Get the variable from the file name
        variable="${file_name##*/}"
        # Replace the underscore with a dot
        variable="${variable//_/.}"

        echo "$variable" >> "$output_file"

        echo "Running with variable = $variable"

        # Run the code for the file
        mvn exec:java -Dexec.mainClass="ar.edu.itba.ss.Main" -Dexec.args="$file"

        python3 graphics/delta_variable.py "$output_file"
    fi
done