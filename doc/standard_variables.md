## Standard variables in CanReg5

### Necessary variables

- Surname (String)
- IncidenceDate (Formatted as yyyyMMdd)
- BirthDate (Formatted as yyyyMMdd)
- Age (2 or 3 digits (with age unknown as 99 or 999))
- Sex (Most registries use 1 and 2, some M and F.)
- Topography (ICD-O-3, only the 3 digits)
- Morphology (ICD-O-3, only the 4 digits)
- Behaviour (ICD-O-3, 1 digit)
- BasisDiagnosis (IACR/IARC standard, 1 character)
- Source1 (Any coding is valid)
- ICD10 (4 characters (C or D followed by 3 characters), auto-generated)
- ICCC (Children classification, (max) 4 characters, auto-generated)
- AddressCode (Any number of characters/digits. (Most registries would have 2 or 3 digits long, though.))

### Recommended variables

- FirstName (String)
- Lastcontact (Date of last contact, Formatted as yyyyMMdd)
- VitalStatus (Alive or dead)
- Grade (ICD-O-3, 1 character)
- Stage (Any coding is valid (as of now))

### System variables

- CheckStatus (1 character, Tumour)
- TumourRecordStatus (1 character)
- PatientCheckStatus (1 character)
- PatientRecordStatus (1 character)
- PatientID (Most registries would have 8 digits, some characters and some longer)
- TumourID (Lenght Patient ID + 4)
- PatientIDTumourTable (The ID of the patient in the tumour table)
- PatientRecordID (The ID of the patient record)
- PatientRecordIDTumourTable (The ID of the patient record in the tumour table)
- PatientUpdateDate (Formatted as yyyyMMdd)
- TumourUpdateDate (Formatted as yyyyMMdd)
- PersonSearch (1 character)
- MultPrimSeq (Sequence number)
- MultPrimTot (Total number of cancers)
- ObsoleteFlagTumourTable (1 character)
- ObsoleteFlagPatientTable (1 character)
- TumourUnduplicationStatus (1 character)
- TumourIDSourceTable (The ID of the tumour record in the source table)
- SourceRecordID (The ID of the source record)
- PatientUpdatedBy (String)
- TumourUpdatedBy (String)

### Deprecated/legacy variables

- MultPrimCode (3 characters, deprecated)
- Source2 (Any coding is valid, deprecated)
- Source3 (Any coding is valid, deprecated)
- Source4 (Any coding is valid, deprecated)
- Source5 (Any coding is valid, deprecated)
- Source6 (Any coding is valid, deprecated)
