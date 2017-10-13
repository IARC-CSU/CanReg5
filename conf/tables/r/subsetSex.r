subsetSex <- function(data, sex){

subsetSex <- subset(data, data$SEX == sex)

return(subsetSex)

}#End function subsetSex