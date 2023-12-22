import numpy as np
from scipy.interpolate import RectBivariateSpline
import matplotlib.pyplot as plt

MIN_RADIUS = 0.7
MAX_RADIUS = 0.9
RADIUS_AMPLITUDE = 0.12

MIN_LENGTH = 0
MAX_LENGTH = 3.5

LENGTH_STEP = 0.5
RADIUS_STEP = 0.05

class MomentOfInertiaCalculator:
    INTERPOLATION_FUNCTION = None

    @staticmethod
    def is_within(length, radius, x, y):
        if -length/2 <= x <= length/2:
            return True

        dx = np.abs(x) - length/2  # distance from center of circle
        return np.sqrt(dx*dx + y*y) <= radius

    @staticmethod
    def calculate_moment_of_inertia(length, radius, step=0.01):
        tot = 0
        max_x = length / 2 + radius
        count = 0
        for x in np.arange(-max_x, max_x, step):
            for y in np.arange(-radius, radius, step):
                if not MomentOfInertiaCalculator.is_within(length, radius, x, y):
                    continue

                tot += x*x + y*y
                count += 1

        return tot / count if count > 0 else 0

    @staticmethod
    def interpolate(lengths, radiuses, moments_of_inertia):
        interpolator = RectBivariateSpline(lengths, radiuses, moments_of_inertia)
        return interpolator

    @staticmethod
    def get_interpolating_function(lengths, radiuses, moments_of_inertia):
        print("Interpolating moments of inertia")
        interpolating_function = MomentOfInertiaCalculator.interpolate(lengths, radiuses, moments_of_inertia)

        print("Done interpolating moments of inertia")
        # Interpolate to obtain a continuous function
        return interpolating_function

calculator = MomentOfInertiaCalculator()

lengths = np.arange(MIN_LENGTH, MAX_LENGTH + LENGTH_STEP / 2, LENGTH_STEP)
radiuses = np.arange(MIN_RADIUS - RADIUS_AMPLITUDE, MAX_RADIUS + RADIUS_AMPLITUDE + RADIUS_STEP / 2, RADIUS_STEP)

moments_of_inertia = np.zeros((len(lengths), len(radiuses)))
for i, length in enumerate(lengths):
    for j, radius in enumerate(radiuses):
        moments_of_inertia[i, j] = calculator.calculate_moment_of_inertia(length, radius)

interpolation_function = calculator.get_interpolating_function(lengths, radiuses, moments_of_inertia)

mean_radius = np.mean(radiuses)
print("Mean radius: {}".format(mean_radius))

numeric_lengths_plot, numeric_radiuses_plot = np.meshgrid(lengths, mean_radius)
numeric_moments_of_inertia_plot = np.vectorize(calculator.calculate_moment_of_inertia)(numeric_lengths_plot, numeric_radiuses_plot)

moments_of_inertia_plot = interpolation_function.ev(lengths, [mean_radius] * len(lengths))

# Plot interpolated function
plt.plot(lengths, moments_of_inertia_plot)

# Plot numeric points
plt.scatter(numeric_lengths_plot, numeric_moments_of_inertia_plot, c='r', marker='o')

plt.xlabel('Longitud (cm)')
plt.ylabel('Momento de Inercia (g * cm²)')

plt.savefig("outFiles/moment_of_inertia.png")

# Calculate error

# Calculate numeric value for new points
lengths = np.arange(MIN_LENGTH + LENGTH_STEP / 2, MAX_LENGTH + LENGTH_STEP / 2, LENGTH_STEP)
radiuses = np.arange(MIN_RADIUS - RADIUS_AMPLITUDE + RADIUS_STEP / 2, MAX_RADIUS + RADIUS_AMPLITUDE + RADIUS_STEP / 2, RADIUS_STEP)

lengths, radiuses = np.meshgrid(lengths, radiuses)

numeric_values = np.vectorize(calculator.calculate_moment_of_inertia)(lengths, radiuses)
# calculate interpolation for each numeric point
interpolation_values = interpolation_function.ev(lengths, radiuses)

# Calculate error for each point
error = np.abs(numeric_values - interpolation_values)

# Calculate mean squared error
mean_squared_error = np.mean(error * error)
print("Mean squared error: {}".format(mean_squared_error))
