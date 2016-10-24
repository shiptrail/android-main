#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""
- am schluss alles mit matplotlib und mapnik / kmlpy runterrendern
 http://stackoverflow.com/questions/15691525/python-mapnik-example-on-how-to-render-a-map-with-a-gps-track-on-it
 http://kartograph.org/
- die zeiten mit matplotlib als histogramme / bin-basiert
- die orte als linie auf die karte mit farben für "räudiges netz / gutes netz" "viele gebatchte punkte / wenig gebatchte punkte"

netzwerk modus pro zeit -> barplot / zeitverlauf schneller nach oben -> modi rausfiltern udn numerisch kodieren
dito pro ort, zeit=farbe + auf karte

generell: welche verschiedenen netzmodi wurden getroffen und welcher hatte welchen anteil -> torte

(anzahl datenpunkte pro sende versuch: pro zeit -> eher weniger)
anzahl datenpunkte pro sende versuch: pro ort -> könnte man mit farbe von netzwerk koppeln, anzahl=dicke der linie

parameter size + netzwerk -> ähnlich wie mit anzahl letzte zeile, size=linien dicke
parameter network time + netzwerk -> siehe letze zeile

bat perc über zeit

evtl. die linien beider telefone nebeneinander legen


- nicht numerische responsecodes / fehler handeln -> eigener balken
"""

import re
import sys
import json

import matplotlib
import numpy

import simplekml

import matplotlib.pyplot as plt
import numpy as np

from pprint import pprint

base = 'motorola/'
#base = 'samsung-s5/'
#base = ''

file_idx_loc = base+'index_position.json'
file_idx_time = base+'index_time.json'
file_lst_coords = base+'list_coords.json'

idx_loc = None
idx_time = None
lst_coords = None


def bins(key, numeric=True, fl = False):
    types = {}
    for k,v in idx_time.iteritems():
        type = v[key]
        if numeric:
            if type != '':
                type = int(type)
            else:
                type = -1
        if fl:
            if type != '':
                type = float(type)
            else:
                type = -1.0

        if type in types.keys():
            val = types[type]
            types[type] = val+1
        else:
            types[type] = 1
    return types


def make_np_array(data):
    names = ['id', 'data']
    formats = ['f8', 'f8']
    dtype = dict(names = names, formats=formats)
    array = np.fromiter(data.iteritems(), dtype=dtype, count=len(data))
    print(repr(array))
    return array


def reducebins(data,newbins):
    result = {bin: 0 for bin in newbins}
    for key, value in data.iteritems():
        for bin in newbins:
            if key <= bin:
                result[bin] = result[bin]+value
                break
    return result


def barplot(data,xlabel,title):
    x = data.keys()
    y = data.values()
    #width=1/1.5
    plt.bar(x, y, align='center', alpha=0.5)
    y_pos = np.arange(len(x))
    plt.xticks(y_pos,x)
    plt.ylabel('Haeufigkeit')
    plt.xlabel(xlabel)
    plt.title(title)
    plt.gcf()
    #plt.show()
    plt.savefig(title)


if __name__ == '__main__':
    with open(file_idx_loc, 'r') as f:
        idx_loc = json.load(f)
    with open(file_idx_time, 'r') as f:
        idx_time = json.load(f)
    with open(file_lst_coords, 'r') as f:
        lst_coords = json.load(f)

    accuracy = bins('accuracy')
    battperc = bins('battperc', False, True)
    networkparamsize = bins('networkparamsize')
    red_nwparamsize = reducebins(networkparamsize, [3200, 3300, 3400, 3700, 4200, 6000, 7000, 10000, 15000, 100000, 300000, 400000])
    networktime = bins('networktime')
    nwtime2 = networktime
    #nwtime2.pop(-1)
    red_nwtime = reducebins(networktime, [-1,1500,3000,6000,9000,12000,15000,18000])
    networktype = bins('networktype', False)
    numevents = bins('numevents')


    result = 0
    for k,v in idx_time.iteritems():
        result += int(v['networkparamsize'])
    print('total volume consumed:{}'.format(result))

    print('accuracy: '+repr(accuracy))
    print('battperc: '+repr(battperc))
    print('networktime: len: {} min {}, max: {}'.format(len(networktime), min(nwtime2), max(networktime)))
    print('nwtime reduced: '+repr(red_nwtime))
    print('networkparamsize: len: {} min {}, max: {}'.format(len(networkparamsize), min(networkparamsize), max(networkparamsize)))
    print('nwparamsize reduced: ' + repr(red_nwparamsize))
    print('networktypes: '+repr(networktype))
    print('numevents: '+repr(numevents))

    #gaussian_numbers = np.random.randn(1000)
    #print(repr(gaussian_numbers))
    #plt.hist(gaussian_numbers)
    #plt.title("Gaussian Histogram")
    #plt.xlabel("Value")
    #plt.ylabel("Frequency")

    #plt.gcf()
    #plt.show()

    barplot(red_nwtime,'Uebertragungszeit in ms','Netzwerkuebertragungszeit')