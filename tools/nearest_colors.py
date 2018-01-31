import math
import sys

'''
Simple script that will find the closest match to a color in the hardcoded list, (i.e. color rounding).
Replace the colors in the 'allowed_colors' list with your acceptable colors, then simply
'python nearest_colors.py <color_hex>' and it will print the nearest match.

Adapted from: https://stackoverflow.com/questions/34366981/python-pil-finding-nearest-color-rounding-colors
'''

allowed_colors = [
    'F5F5F5',
    'e6e6e6',
    'e0e0e0',
    'c7c7c7',
    '999999',
    '757575',
    '616161',
    '404040',
    '1F1F1F',
]

'''
    <color name="gray100">#F5F5F5</color>
    <color name="gray200">#e6e6e6</color>
    <color name="gray300">#e0e0e0</color>
    <color name="gray400">#c7c7c7</color>
    <color name="gray500">#999999</color>
    <color name="gray600">#757575</color>
    <color name="gray700">#616161</color>
    <color name="gray800">#404040</color>
    <color name="gray900">#1F1F1F</color>
'''


def hex_to_rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def distance(c1, c2):
    (r1,g1,b1) = c1
    (r2,g2,b2) = c2
    dist = math.sqrt((r1 - r2)**2 + (g1 - g2) ** 2 + (b1 - b2) **2)
    #print(c1,c2,dist)
    return(dist)


if __name__ == '__main__':
    input_color = sys.argv[1]
    input_rgb = hex_to_rgb(input_color)

    closest_colors = sorted(allowed_colors, key=lambda color: distance(input_rgb, hex_to_rgb(color)))
    closest_color = closest_colors[0]
    print(closest_color)
