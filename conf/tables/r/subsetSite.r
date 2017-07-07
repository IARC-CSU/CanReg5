subsetSite <- function(data, site){

subsetSite <- subset(data, data$ICD10GROUP == site)

return(subsetSite)

}#End function subsetSite