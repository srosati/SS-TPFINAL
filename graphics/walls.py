width = 20
height = 70
slit = 5
curr_id = 501
max_iter = 100
with open('../outFiles/walls.txt', 'w') as wall_file:
    for i in range(0, max_iter):
        wall_file.write(str(2 * (width + height) - slit) + "\n")
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
