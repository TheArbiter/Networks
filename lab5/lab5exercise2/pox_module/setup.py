#!/usr/bin/env python
'''Setuptools params'''

from setuptools import setup, find_packages

setup(
    name='comp9331',
    version='0.0.0',
    description='Network controller for UNSW COMP3331 Labs',
    author='COMP3331',
    author_email='mrezvani@cse.unsw.edu.au',
    url='http://www.cse.unsw.edu.au/~cs3331/',
    packages=find_packages(exclude='test'),
    long_description="""\
Insert longer description here.
      """,
      classifiers=[
          "License :: OSI Approved :: GNU General Public License (GPL)",
          "Programming Language :: Python",
          "Development Status :: 1 - Planning",
          "Intended Audience :: Developers",
          "Topic :: Internet",
      ],
      keywords='UNSW COMP3331',
      license='GPL',
      install_requires=[
        'setuptools',
        'twisted',
      ])

