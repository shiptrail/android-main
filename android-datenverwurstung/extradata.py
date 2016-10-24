#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""
- daten öffnen
- line by line einlesen
- gegen regex matchen, entsprechend weiterverwenden oder ignoren
- lokal mitschleifen
    - bat perc
    - mobilfunk typ
    - online / offline
    - tracking
- basis-datenstruktur dict x2
key
    - zeit
    - ort
nach
    - funktyp
    - ort (lat/lng ->tupel)
    - zeit / ort (je den anderen)
    -
- beide nach json runterdumpen

- set: mobilfunk typen -> wieviele mobilfunktypen wurden gesehen
- dict: mobilfunktyp -> anzahl -> oder später aus dem hauptdict filtern


- am schluss alles mit matplotlib und mapnik / kmlpy runterrendern
 http://stackoverflow.com/questions/15691525/python-mapnik-example-on-how-to-render-a-map-with-a-gps-track-on-it
 http://kartograph.org/
- die zeiten mit matplotlib als histogramme / bin-basiert
- die orte als linie auf die karte mit farben für "räudiges netz / gutes netz" "viele gebatchte punkte / wenig gebatchte punkte"
"""

import re
import sys
import json
import colors

fn = 'information.txt'
fnd = 'short.txt'


# 08.10.2016 11:18:57:345 GMT+02:00:
# re_base = "^\d{2}\.\d{2}\.\d{4} \d{2}:\d{2}:\d{2}:\d{3} GMT\+\d{2}:\d{2}: "
base = "^(?P<date>\d{2}\.\d{2}\.\d{4}) (?P<time>\d{2}:\d{2}:\d{2}):\d{3} GMT\+\d{2}:\d{2}: "
battperc = base+"Battery Percent: (?P<battperc>[\d\.]{4})"
trackstatus = base+"Tracking is now (?P<trackstatus>[a-z]*)"
network_status = base+"Network status: (?P<networkstatus>[A-za-z]*)"
network_type = base+"Network Type Information: (?P<networktype>[A-za-z / \+0-9]*)"
# re_network_url = re.compile(re_base+"Network: Url: (?P<networkurl>[A-za-z:.\- / \+0-9]*)")
network_uuid = base+"Network: Url: https://shiptrail.lenucksi.eu/v2/(?P<networkuuid>[a-z-0-9]*)/send"
network_parameter = base+"Network: Parameter: (?P<networkparams>.*)"
network_parameter_size = base+"Network: Parameter Size: (?P<networkparamsize>[0-9]*)"
network_responsecode = base+"Network: Responsecode / Error: (?P<networkresponsecode>[0-9]*)"
network_time = base+"time needed \(empty if failed\): (?P<networktime>[0-9]*)"

#re_base = "^(?P<date>\d{2}\.\d{2}\.\d{4}) (?P<time>\d{2}:\d{2}:\d{2}):\d{3} CEST: " # motorola
re_base = "^(?P<date>\d{2}\.\d{2}\.\d{4}) (?P<time>\d{2}:\d{2}:\d{2}):\d{3} GMT\+\d{2}:\d{2}: " # samsung
re_battperc = re.compile(re_base+"Battery Percent: (?P<battperc>[\d\.]{2,4})")
re_trackstatus = re.compile(re_base+"Tracking is now (?P<trackstatus>[a-z]*)")
re_network_status = re.compile(re_base+"Network status: (?P<networkstatus>[A-za-z]*)")
re_network_type = re.compile(re_base+"Network Type Information: (?P<networktype>.*)")
# re_network_url = re.compile(re_base+"Network: Url: (?P<networkurl>[A-za-z:.\- / \+0-9]*)")
re_network_uuid = re.compile(re_base+"Network: Url: http[s]?://shiptrail.lenucksi.eu/v2/(?P<networkuuid>[a-z-0-9]*)/send")
re_network_parameter = re.compile(re_base+"Network: Parameter: (?P<networkparams>.*)")
re_network_parameter_size = re.compile(re_base+"Network: Parameter Size: (?P<networkparamsize>[0-9]*)")
re_network_responsecode = re.compile(re_base+"Network: Responsecode / Error: (?P<networkresponsecode>.*)")
re_network_time = re.compile(re_base+"Network: time needed \(empty if failed\): (?P<networktime>[0-9]*)")


res = [battperc, trackstatus, network_status, network_type, network_uuid, network_parameter,
       network_parameter_size, network_responsecode, network_time]

re_list = [re_battperc, re_trackstatus, re_network_status, re_network_type, re_network_uuid, re_network_parameter,
           re_network_parameter_size, re_network_responsecode, re_network_time]

re_dict = {'battperc': re_battperc,
           'trackstatus': re_trackstatus,
           'networkstatus': re_network_status,
           'networktype': re_network_type,
           'networkuuid': re_network_uuid,
           'networkparams': re_network_parameter,
           'networkparamsize': re_network_parameter_size,
           'networkresponsecode': re_network_responsecode,
           'networktime': re_network_time}


#full ='|'.join('{}'.format(x) for x in res)
#print full
#re_full = re.compile(full)

'''
Reihenfolge
battery perc
network url
Network param
param size
response code  / error
time needed

-> zwischenspeichern und entsprechend in die dicts einsortieren

zeit der batt perc nehmen
timestamp(s) aus den daten aufzeichnen
anzahl elemente in den daten zählen
sat-infos genauigkeit aufnehmen "accuracy"
lat/lon
'''
# time -> dict
index_time = {}
# (lat,lon) -> dict
index_position = {}

list_coords = []

# Event speichern bis fertig, fertig wenn alles gefunden
cur_event = {}

# network type enum / dict / field
cur_network_type = ''
cur_battery_perc = 0
# False = offline, True = Online
cur_track_status = False
cur_network_status = False


def check_and_finish(event):
    global cur_event, cur_network_type, cur_battery_perc, cur_track_status, cur_network_status
    # entries required
    #print('check and finish: {}'.format(event))
    set_elems = set(sorted(re_dict.keys()))
    set_elems = set_elems.union(sorted(['time', 'lat', 'lng', 'timestamp', 'accuracy', 'numevents']))
    set_elems.remove('networkparams')

    set_elems.remove('trackstatus')
    set_elems.remove('networktype')
    set_elems.remove('networkstatus')

    #print('{}\n{}\nissubset({})\n'.format(set(sorted(event.keys())),set_elems,set_elems.difference(set(event.keys()))))
    if set_elems.issubset(set(event.keys())):
        print(colors.green('current event done: {}'.format(event['timestamp'])))

        result = {}
        result['battperc']= cur_battery_perc
        result['networktype']= cur_network_type
        result['networkstatus']= cur_network_status
        result['trackstatus']= cur_track_status
        result['time'] = event['time']
        result['lat'] = event['lat']
        result['lng'] = event['lng']
        result['timestamp'] = event['timestamp']
        result['accuracy'] = event['accuracy']
        result['numevents'] = event['numevents']
        result['networktime'] = event['networktime']
        result['networkparamsize'] = event['networkparamsize']

        # insert into both data structures
        index_time[result['time']] = result
        index_position['('+str(result['lat'])+','+str(result['lng'])+')'] = result

        # clear temporary event
        cur_event = {}
    else:
        #print(json.dumps(event, sort_keys=True, indent=4, separators=(',', ': ')))
        pass


def dump_index_time(to_file=False):
    if not to_file:
        print(json.dumps(index_time, sort_keys=True, indent=4, separators=(',', ': ')))
    else:
        with open('index_time.json', 'w') as fp:
            json.dump(index_time, fp, sort_keys=True, indent=4, separators=(',', ': '))


def dump_index_location(to_file=False):
    if not to_file:
        print(json.dumps(index_position, sort_keys=True, indent=4, separators=(',', ': ')))
    else:
        with open('index_position.json', 'w') as fp:
            json.dump(index_position, fp, sort_keys=True, indent=4, separators=(',', ': '))
        with open('list_coords.json', 'w') as fp:
            json.dump(list_coords, fp, sort_keys=True, indent=4, separators=(',', ': '))


with open(fn, 'r') as f:
    for line in f:
        # zeile präparieren
        results = {k: v.findall(line) for k, v in re_dict.iteritems()}
        remove_empty = {k: v for k, v in results.iteritems() if v != []}
        #print(line+str(remove_empty))
        # statische resultate durchsuchen + zuweisen -> statisches
        if 'battperc' in remove_empty.keys():
            # {'battperc': [('08.10.2016', '11:18:57', '0.63')]}
            cur_battery_perc = remove_empty['battperc'][0][2]
            cur_event['battperc'] = cur_battery_perc
        if 'networktype' in remove_empty.keys():
            cur_network_type = remove_empty['networktype'][0][2]
            cur_event['networktype'] = cur_network_type
        if 'networkstatus' in remove_empty.keys():
            cur_network_status = remove_empty['networkstatus'][0][2] == 'Online'
            cur_event['networkstatus'] = cur_network_status
        if 'trackstatus' in remove_empty.keys():
            cur_track_status = remove_empty['trackstatus'][0][2] == 'enabled'
            cur_event['trackstatus'] = cur_track_status

        '''
        Reihenfolge
        X battery perc
        X network uuid
        x Network param -> daten, json
        x param size ->
        x response code  / error
        time needed
        '''
        if 'networkuuid' in remove_empty.keys():
            time = remove_empty['networkuuid'][0][1]
            uuid = remove_empty['networkuuid'][0][2]
            cur_event['time'] = time
            cur_event['networkuuid'] = uuid
            check_and_finish(cur_event)
            #print(colors.red('networkuuid:'+line))

        if 'networkparams' in remove_empty.keys():
            time = remove_empty['networkparams'][0][1]
            params = remove_empty['networkparams'][0][2]
            # resultat sollte ein array sein
            jsondata = json.loads(params)
            #print('networkparams json data: \n'+json.dumps(jsondata, sort_keys=True, indent=4, separators=(',', ': ')))
            numevents = len(jsondata)
            print('numevents:{}'.format(numevents))

            for item in jsondata:
                list_coords.append((item['lat'],item['lng'],item['timestamp']))
                print('({},{})@{}'.format(item['lat'],item['lng'],item['timestamp']))

            accuracy = jsondata[0]['gpsMeta'][0]['accuracy']
            lat = jsondata[0]['lat']
            lng = jsondata[0]['lng']
            timestamp = jsondata[0]['timestamp']
            '''
            json:
            array of dict
            dict:
                accelerometer [],
                annotation [],
                compass [],
                ele,
                x gpsMeta [*accuracy*, satCount, toffset],
                x lat,
                x lng,
                orientation [],
                x timestamp
            '''
            cur_event['time'] = time
            cur_event['lat'] = lat
            cur_event['lng'] = lng
            cur_event['timestamp'] = timestamp
            cur_event['accuracy'] = accuracy
            cur_event['numevents'] = numevents
            check_and_finish(cur_event)
        if 'networkparamsize' in remove_empty.keys():
            time = remove_empty['networkparamsize'][0][1]
            paramsize = remove_empty['networkparamsize'][0][2]
            cur_event['time'] = time
            cur_event['networkparamsize'] = paramsize
            #print(colors.yellow('paramsize:'+paramsize))
            check_and_finish(cur_event)
        if 'networkresponsecode' in remove_empty.keys():
            time = remove_empty['networkresponsecode'][0][1]
            responsecode = remove_empty['networkresponsecode'][0][2]
            cur_event['time'] = time
            cur_event['networkresponsecode'] = responsecode
            check_and_finish(cur_event)
        if 'networktime' in remove_empty.keys():
            time = remove_empty['networktime'][0][1]
            networktime = remove_empty['networktime'][0][2]
            cur_event['time'] = time
            cur_event['networktime'] = networktime
            #print(colors.yellow('nwtime:'+networktime))
            check_and_finish(cur_event)
print('processing done, time index: {} position index: {}'.format(len(index_time),len(index_position)))
#dump_index_time()
#dump_index_location()
dump_index_time(True)
dump_index_location(True)