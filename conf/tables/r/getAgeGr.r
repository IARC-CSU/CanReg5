getAgeGr <- function(ageNr){

ageList <- list(before = seq(0, 16), after =  c("0-", "5-", "10-", "15-", "20-", "25-", "30-", "35-", "40-", "45-", "50-", "55-", "60-", "65-", "70-", "75-", "80+"))
ageGr <- ageList$after[ageList$before == ageNr][1]

return(ageGr)

}#End function## 

