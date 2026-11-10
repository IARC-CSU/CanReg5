*[← Previous: 10. Migrating from CanReg4](10-migrating-from-canreg4.md) · [Table of Contents](README.md) · [Next: 12. Technical Details on the Table Builder →](12-table-builder-technical-details.md)*

---

# 11. Advanced Database Management

This appendix covers some advanced database management topics

## The System definition file (the XML file)

### Advanced options in the CanReg5 person search

You have access to a couple of extra settings in the CanReg5 system
definition file to improve the detection of duplicate patients (in
addition to *weight*), namely *discriminatory power, reliability*, and
*presence*.

#### Weight

The weight of the link. These can add up to more/less than 100 (but will
then be normalized to 100). The weight is the score that the link will
get if there is a 100% match on that variable. Some algorithms also
return numbers between 0 and a 100, and if so, the score is adjusted.

#### Disciminatory power

Does nothing at the moment!

(Discriminatory Power calculator:
<http://insilico.ehu.es/mini_tools/discriminatory_power/index.php>)

#### Reliablity

Does nothing at the moment!

How reliable is the data. Scale from 0 to 1.

#### Prescence

Does nothing at the moment!

How often is the variable filled. Scale from 0 to 1.

#### Threshold

Set between 0 and 100. Cases with higher match percentage than this
shows up in person search.

---

*[← Previous: 10. Migrating from CanReg4](10-migrating-from-canreg4.md) | [Table of Contents](README.md) | [Next: 12. Technical Details on the Table Builder →](12-table-builder-technical-details.md)*
