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


# IMPORTANT: stationary_dic must be updated manually
stationary_dic = {
    "2": 20,
    "3": 0,
    "4": 0,
    "5": 62,
    "6": 0,
}

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
        plt.ylabel("Caudal (1/s)")
        plt.plot(flow_x, flow, '-')

        plt.savefig(output_path + variable + ".png")
        plt.clf()

        print("variable = {}".format(variable))
        print("Points: {}".format(len(flow)))

        start_idx = stationary_dic[str(variable)]
        subflow = flow[start_idx:]
        print("start_idx = {}".format(start_idx))

        mean = np.mean(subflow)
        std = np.std(subflow, ddof=1)
        print("mean = {}".format(mean))
        print("std = {}".format(std))

        variable_values.append(variable)
        mean_values.append(np.mean(subflow))
        std_values.append(np.std(subflow, ddof=1))


    # Plot means and stds
    plt.xlabel(x_label)
    plt.ylabel("Caudal (1/s)")
    plt.errorbar(x=variable_values, y=mean_values, yerr=std_values, fmt='o-', capsize=5)
    plt.yticks(np.arange(0, 2.5, 0.3))

    plt.savefig(output_path + "means.png")


input_file.close()
