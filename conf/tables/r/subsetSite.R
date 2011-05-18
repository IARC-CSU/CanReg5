subsetSite <- function(data, site){

subsetSite <- subset(data, data$ICD10 == site)

return(subsetSite)



}#End function subsetSite