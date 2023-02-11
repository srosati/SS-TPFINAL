import math
print("Generating ovito file...")
with open("../outFiles/out.txt", "r") as out_file:
    with open("../outFiles/ovito.txt", "w") as ovito_file:
        line = out_file.readline()
        [slit, width, height] = line.split(' ')
        line = out_file.readline()
        while line:
            if len(line.split(" ")) <= 2:
                ovito_file.write(line)
            else:
                [particle_id, x, y, w, r, l] = line.split(' ')
                w1 = math.cos(math.pi/4)
                x1 = math.sin(math.pi/4)
                z1 = 0
                y1 = 0

                w2 = math.cos((math.pi/2 - float(w))/2)
                x2 = 0.0
                y2 = -math.sin((math.pi/2 - float(w))/2)
                z2 = 0.0

                quart_w = w1*w2 - x1*x2 - y1*y2 - z1*z2
                quart_x = w1*x2 + x1*w2 + y1*z2 - z1*y2
                quart_y = w1*y2 - x1*z2 + y1*w2 + z1*x2
                quart_z = w1*z2 + x1*y2 - y1*x2 + z1*w2

                ovito_file.write("{} {} {} {} {} {} {} {} {} {}\n".format(particle_id, x, y, r, float(l), quart_w,
                                                                       quart_x, quart_y, quart_z, w))
            line = out_file.readline()

    ovito_file.close()
out_file.close()

print("Generating walls...")
slit = float(slit)
width = int(float(width))
height = int(float(height))
# print("Slit size: " + str(slit))
# width = 20
# height = 70
curr_id = 501
max_iter = 100
with open('../outFiles/walls.txt', 'w') as wall_file:
    for i in range(0, max_iter):
        wall_file.write(str(int(2 * (width + height) - slit)) + "\n")
        wall_file.write("position\n")
        # Horizontal walls
        for x in range(0, width):
            if (x <= (width/2 - slit/2)) or (x >= (width/2 + slit/2)):
                wall_file.write(str(curr_id) + " " + str(x) + " " + str(0) + " 0.3\n")
                curr_id += 1

            wall_file.write(str(curr_id) + " " + str(x) + " " + str(height) + " 0.3\n")
            curr_id += 1

        # Vertical walls
        for y in range(0, height):
            wall_file.write(str(curr_id) + " " + str(0) + " " + str(y) + " 0.3\n")
            curr_id += 1
            wall_file.write(str(curr_id) + " " + str(width) + " " + str(y) + " 0.3\n")
            curr_id += 1

        curr_id = 501

wall_file.close()