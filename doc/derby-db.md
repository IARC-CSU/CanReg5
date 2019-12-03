# CanReg5 Database #

The database in CanReg5 is ApacheDB/Derby.

## Tools ##

Here are some tools that can help you while working with this database outside of CanReg5.

### ij ###

Download from http://db.apache.org/derby/derby_downloads.html 

Change to folder with CanReg5 database, for example: 

```bash
cd C:\Users\morten\.CanReg-Server\Database\TRN-E
```

Run 'ij' (make sure it is in the PATH -- or provide the complete path)

```bash
ij
```

Connect to database...

```sql
connect 'jdbc:derby:.';
```

Now you can execute SQL statements directly. (Always terminate by ';'.)

#### Examples ####

- Show all records in PATIENT TABLE

```sql 
SELECT * FROM PATIENT;
```

- Add a column TNM to table TUMOUR

```sql
ALTER TABLE TUMOUR ADD COLUMN TNM VARCHAR(10)
```

---

List of ij-commands can be found here: https://db.apache.org/derby/docs/10.15/tools/ctoolsijcomref23268.html