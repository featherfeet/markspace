#!/usr/bin/env python3

import matplotlib.pyplot as plt

line1, = plt.plot([0, 100], [1, 30])
line2, = plt.plot([0, 100], [10, 14])
plt.xlabel("Number of Student Answers")
plt.ylabel("Grading Time (mins) (higher is worse)")
plt.legend((line1, line2), ("Low b, high m", "High b, low m"))

plt.show()
