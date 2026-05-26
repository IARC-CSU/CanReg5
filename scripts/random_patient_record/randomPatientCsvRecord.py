import csv
import pandas as pd
import random
import datetime

'''
    A script to generate random patient records from a dictionary (source csv)
    by wcheninfotel
    Feb 2023
'''


# a source csv with the dictionary (could be a csv of records exported from CanReg)
sourceCsv = "tumours_csv_test.csv"
# the target file in which generated records will be stored 
resultFile = "records/randomPatientRecord_2018.csv"
target = open(resultFile, 'w', newline='')

# get dictionary from source csv  
df = pd.read_csv(sourceCsv, on_bad_lines="skip")
firstNames = df["First names"].dropna().tolist()
famName = df["Family Name"].dropna().tolist()
# addresses = df["Address"].dropna().tolist()
races = df["Race"].dropna().tolist()
occupations = df["Occupation"].dropna().tolist()


'''
    ID's pattern

    2023xxxx|xx|xx|xx
    patientId=registryNumber|patientRecordId|tumourId|sourceId

    ex: 
    patientId = 20231234
    patientRecordId = 2023123401
    tumourId1 = 202312340101
    tumourId2 = 202312340102
    sourceId = 20231234010101
'''


id = 1 # from this id we start
recordNUm = 9999 # number of record to generate (shouldn't exced 9999 per execution)

# target csv header 
writer = csv.writer(target)
writer.writerow(
    ["regno",
    "pers",
    "stat",
    "patientcheckstatus",
    "race",
    "causeofdeath",
    "sex",
    "patientupdatedby",
    "deathdate",
    "patientrecordstatus",
    "civils",
    "maidn",
    "obsoleteflagpatienttable",
    "famn",
    "firstn",
    "midn",
    "occu",
    "patientupdatedate",
    "dlc",
    "patientrecordid",
    "birthd"])


def createRow(nid):

    today = datetime.date.today()
    year = "2018" #today.strftime("%Y")
    patientId = str(year) + str(id).rjust(4,"0")
    recordStatus = 1
    checkStatus = 1
    age = random.randint(0, 120)
    #address = int(random.choice(addresses))
    updateDate = today
    updateDateStr = updateDate.strftime('%Y%m%d')
    obsoleteFlagTumourTable = ""
    patientRecordId = patientId + "01"
    tumourId = patientRecordId + "01"
    personSerach = 1
    fName = random.choice(firstNames)
    famN = random.choice(famName)
    sex = random.randint(1,2)
    date2020 = datetime.date(2020, random.randint(1,12), random.randint(1,28))
    birthDate = date2020.replace(date2020.year - age)
    birthDateStr = birthDate.strftime('%Y%m%d')
    race = random.choice(races)
    occupation = random.choice(occupations)
    sourceId = tumourId + "01" 
    civil = ""
    maidn = ""
    midN = ""
    deathDay= ""

    nid = nid + 1

    r = [patientId, personSerach, "", checkStatus, race, "", sex, "morten", deathDay, recordStatus, civil, maidn, obsoleteFlagTumourTable, famN, fName, midN, occupation, updateDateStr, "", patientRecordId, birthDateStr]
    
    return r, nid
    

for i in range(recordNUm):
    csvRow, id = createRow(id)
    writer.writerow(csvRow)










