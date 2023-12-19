
import sys

if len(sys.argv) < 2:
    print("Usage: python3 delta_variable.py <output_file>")
    exit()

# Get the output file name
output_file = sys.argv[1]

# This program takes the output of the flow.txt file and creates a new file
# aggregating the flow over time (total amount of particles that have left at a certain point in time)
# it is independent from the output file itself, but before running this program, the output file
# must have the value of the variable to analyze appended to it
# Then this program will append the aggregated flow
# A different program will then plot the aggregated flow for each variable value

tot = 0
with open('./outFiles/flow.txt', 'r') as flow_file:
    with open(output_file, 'a') as output_file:
        line = flow_file.readline()
        while line:
            [step, flow] = line.split(' ')
            tot += int(flow)
            output_file.write("{} {}\n".format(step, tot))
            line = flow_file.readline()