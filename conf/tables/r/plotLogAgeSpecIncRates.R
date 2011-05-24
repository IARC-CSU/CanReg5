plotLogAgeSpecIncRates <- function(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable){

xlabel <- "Age"
ylabel <- "Log rates"

colM <- colors()[28]
colF <- colors()[554]

age<- seq(0, (nrOfAgeGroups - 1), 1)

if(!is.data.frame(dataFemaleRates)){

rates <- dataMaleRates$RATESper100000
rates[rates == 0] <- 1
rates <- log(rates)

tableData <- cbind(dataMaleRates, rates)
tableData$ICD10GROUPLABEL <- substr( as.vector(tableData$ICD10GROUPLABEL)[1] , 4, nchar( as.vector( tableData$ICD10GROUPLABEL)[1] ) )
colnames(tableData) <- c(colnames(dataMaleRates), "logRATES")
makeTable(tableData, outFileTable)

yMaxMin <- range(c(0, rates))
period <- dataMaleRates$YEAR[1]
#ratesPer100000 <- as.vector(dataMaleRates$RATES)*100000
plot(age, rates, xlab = xlabel, ylab = ylabel, col = colM, axes = FALSE, ylim = yMaxMin, lwd =2, type = 'l')
axis(2, tck = 1, col = "grey", lty = "dotted")
axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
box()
legend("topleft",inset = 0.01, "Male", col = colM, lty = 1, lwd =2, bg = "white")


}else if(!is.data.frame(dataMaleRates)){

rates <- dataFemaleRates$RATESper100000
rates[rates == 0] <- 1
rates <- log(rates)

tableData <- cbind(dataFemaleRates, rates)
tableData$ICD10GROUPLABEL <-  substr( as.vector(tableData$ICD10GROUPLABEL)[1] , 4, nchar( as.vector( tableData$ICD10GROUPLABEL)[1] ) )
colnames(tableData) <- c(colnames(dataFemaleRates), "logRATES")
makeTable(tableData, outFileTable)

yMaxMin <- range(c(0, rates))
period <- dataFemaleRates$YEAR[1]
#ratesPer100000 <- as.vector(dataFemaleRates$RATES)*100000
plot(age, rates, xlab = xlabel, ylab = ylabel, col = colF, axes = FALSE, ylim = yMaxMin, lwd =2, type = 'l')
axis(2, tck = 1, col = "grey", lty = "dotted")
axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
box()
legend("topleft",inset = 0.01, "Female", col = colF, lty = 1, lwd =2, bg = "white")


}else {


ratesFemale <- dataFemaleRates$RATESper100000
ratesFemale[ratesFemale == 0] <- 1
ratesFemale <- log(ratesFemale)

ratesMale <- dataMaleRates$RATESper100000
ratesMale[ratesMale == 0] <- 1
ratesMale <- log(ratesMale)


#For males
tableDataM <- cbind(dataMaleRates, ratesMale)
tableDataM$ICD10GROUPLABEL <-  substr( as.vector(tableDataM$ICD10GROUPLABEL)[1] , 4, nchar( as.vector( tableDataM$ICD10GROUPLABEL)[1] ) )
colnames(tableDataM) <- c(colnames(dataMaleRates), "logRATES")
makeTable(tableDataM, outFileTable)

#For females
tableDataF <- cbind(dataFemaleRates, ratesFemale)
tableDataF$ICD10GROUPLABEL <-  substr( as.vector(tableDataF$ICD10GROUPLABEL)[1] , 4, nchar( as.vector( tableDataF$ICD10GROUPLABEL)[1] ) )
colnames(tableDataF) <- c(colnames(dataFemaleRates), "logRATES")
makeTable(tableDataF, outFileTable)

period <- dataMaleRates$YEAR[1]

yMaxMin <- range(c(0, ratesMale, ratesFemale))


plot(age, ratesMale, xlab = xlabel, ylab = ylabel, col = colM, ylim = yMaxMin, xlim =range(c(0:16)), type = 'l', lwd =2, axes = FALSE)
lines(age, ratesFemale, lwd =2, col = colF)
axis(2, tck = 1, col = "grey", lty = "dotted")
axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
box()

legend("topleft",inset = 0.01, c("Male", "Female"), col = c(colM, colF), lty = c(1, 1), lwd =2, bg = "white", )

}#End if else if


caption <- paste("Age-specific incidence rates per 100,000 in ", period, sep = "")
caption <- paste(caption, " \n", sep = "")
caption <- paste(caption, site, sep = "")

title(caption, cex = 0.05)


}#End function plotAgeSpecIncRates