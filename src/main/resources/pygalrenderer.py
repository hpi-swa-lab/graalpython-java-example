#
# Copyright (c) 2021 Software Architecture Group, Hasso Plattner Institute
#
# Licensed under the MIT License.
#

import polyglot
import java
import math
import pygal
import re


# The PygalRenderer has only one method. Instances of this class are meant to be
# used from Java using the GraphRenderer interface. The "render" method declared
# in that interface returns a java.io.InputStream, so we have to do the same
# here.
class PygalRenderer:
    def render(self, func, steps):
        # Render the function "func" discretized to "steps" datapoints
        print(f"Rendering", func, "with", steps, "steps")
        values = []
        x = -100
        while x < 100:
            values.append(eval(func, globals={"x": x, **math.__dict__}))
            x += 200 / steps
        chart = pygal.Line(fill=False)
        chart.add(f"f(x) = {func}", values)
        svg_data = chart.render()

        # The Batik library does not support rgba() values in CSS. We work
        # around this limitation by replacing those CSS values with hexadecimal
        # values.
        svg_data = re.sub(
            b"rgba\((\d+),\s?(\d+),\s?(\d+),\s?[.0-9]+\)",
            lambda m: bytes("#{:02x}{:02x}{:02x}".format(int(m.group(1)), int(m.group(2)), int(m.group(3))), "ascii"),
            svg_data
        )

        # Instantiate a SVGInputStream, a Python subclass of java.io.InputStream
        stream = SVGInputStream()
        # The "stream" object here is an actual Java object. Just like with
        # Java, we cannot simply add attributes to this object. To make this
        # work nicer with Python, Python subclasses of Java objects always have
        # "this" attribute, which refers to the Python extension part of the
        # object, and on this part we can add additional values, just like with
        # pure Python objects.
        stream.this.bytestream = iter(svg_data)
        return stream


# A Python subclass of java.io.InputStream. Note that there is no check at any
# time if the Python subclass implements all abstract methods of the Java
# superclass. Missing methods will simply fail at runtime.
class SVGInputStream(java.io.InputStream):
    # This method returns the next byte in the Python byte stream. Python bytes
    # are unsigned, however, and Java bytes are signed. So we need to convert
    # unsigned values to signed. This is to ensure that the Java side can use
    # the values as bytes. Since Python does not distinguish byte from int, the
    # Java side needs to check if a Python int value is in the Java byte range
    # before using it, and it cannot determine if an out-of-range value is
    # allowed to be cast down. So we have to do it manually here.
    def next_signed_byte(self):
        b = next(self.bytestream)
        if b > 127:
            return b - 256
        else:
            return b

    # The InputStream#read method is the only abstract method we need to
    # provide. However, there is also InputStream#read(byte[]) and
    # InputStream#read(byte[], int off, int len). Unfortunately, Python methods
    # do not have function overloading, so adding the read method here will
    # override all three Java "read" methods. Thus, we need to implement all
    # three cases inside this one Python function.
    def read(self, *args):
        if len(args) == 0:
            # InputStream#read()
            try:
                return next(self.bytestream)
            except StopIteration:
                return -1
        elif len(args) == 1:
            # InputStream#read(byte[])
            byteary = args[0]
            for i in range(len(byteary)):
                try:
                    byteary[i] = self.next_signed_byte()
                except StopIteration:
                    if i == 0:
                        return -1
                    else:
                        return i
            return len(byteary)
        elif len(args) == 3:
            # InputStream#read(byte[], int off, int len)
            byteary, off, length = args
            for i in range(length):
                try:
                    byteary[i + off] = self.next_signed_byte()
                except StopIteration:
                    if i == 0:
                        return -1
                    else:
                        return i
            return length
        else:
            raise TypeError(f"Unexpected arguments {args}")


# We export the PygalRenderer class to Java as our explicit interface with the
# Java side
polyglot.export_value("PygalRenderer", PygalRenderer)
