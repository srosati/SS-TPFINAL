import numpy as np

import matplotlib
matplotlib.use('agg')

from matplotlib import pyplot as plt
import sys

if len(sys.argv) < 4:
    print("Usage: python3 plot_delta_flow_n.py <input_file> <output_path> <x_label>")
    exit()

input_file = sys.argv[1]
output_path = sys.argv[2]
x_label = sys.argv[3]

dn = 10

with open(input_file, "r") as input_file:
    line = input_file.readline()

    variable_values = []
    mean_values = []
    std_values = []
    while line:
        target = dn
        prev_y = 0
        prev_x = -1
        flow = []

        variable = line[:-1]
        line = input_file.readline()
        parts = line.split(" ")
        xs = []
        ys = []
        while len(parts) == 2:
            y = float(parts[0])  # t
            x = int(parts[1])  # n

            if x != prev_x:
                for i in range(prev_x + 1, x + 1): # hasta x inclusive
                    xs.append(i)
                    ys.append(y)

                prev_x = x
            
            line = input_file.readline()
            parts = line.split(" ")

        flow_x = []
        for i in range(dn, len(ys)):
            val = ys[i] - ys[i - dn]
            flow_x.append(ys[i])
            flow.append((xs[i] - xs[i - dn]) / val)

        plt.xlabel("Tiempo (s)")
        plt.ylabel("Q (1/s)")
        plt.plot(flow_x, flow, '-')

        plt.savefig(output_path + variable + ".png")
        plt.clf()


        mean = np.mean(flow[15:])
        std = np.std(flow[15:], ddof=1)
        print("variable = {}".format(variable))
        print("mean = {}".format(mean))
        print("std = {}".format(std))

        variable_values.append(variable)
        mean_values.append(np.mean(flow[15:]))
        std_values.append(np.std(flow[15:], ddof=1))


    # Plot means and stds
    plt.xlabel(x_label)
    plt.ylabel("Q (1/s)")
    plt.errorbar(x=variable_values, y=mean_values, yerr=std_values, fmt='o-', capsize=5)
    plt.savefig(output_path + "means.png")


input_file.close()
