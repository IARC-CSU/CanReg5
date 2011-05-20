plotAgeSpecIncRates <- function(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel){


xlabel <- "Age"
ylabel <- "Log rates"

colM <- colors()[28]
colF <- colors()[554]

age<- seq(0, (nrOfAgeGroups - 1), 1)

if(!is.data.frame(dataFemaleRates)){
yMaxMin <- range(c(0, dataMaleRates$RATES))
period <- dataMaleRates$YEAR[1]
rates <- dataMaleRates$RATES
rates[rates==0] <- 1


plot(age, rates, xlab = xlabel, ylab = ylabel, col = colM, axes = FALSE, ylim = yMaxMin, lwd =2, type = 'l')
axis(2, tck = 1, col = "grey", lty = "dotted")
axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
box()
legend("topleft",inset = 0.01, "Male", col = colM, lty = 1, lwd =2, bg = "white")


}else if(!is.data.frame(dataMaleRates)){
yMaxMin <- range(c(0, dataFemaleRates$RATES))
period <- dataFemaleRates$YEAR[1]

rates <- dataFemaleRates$RATES
rates[rates==0] <- 1

plot(age, (rates), xlab = xlabel, ylab = ylabel, col = colF, axes = FALSE, ylim = yMaxMin, lwd =2, type = 'l')
axis(2, tck = 1, col = "grey", lty = "dotted")
axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
box()
legend("topleft",inset = 0.01, "Female", col = colF, lty = 1, lwd =2, bg = "white")


}else {
period <- dataMaleRates$YEAR[1]

yMaxMin <- range(c(0, dataMaleRates$RATES, dataFemaleRates$RATES))

ratesMale <- dataMaleRates$RATES
ratesFemale <- dataFemaleRates$RATES
ratesMale[rates==0] <- 1
ratesFemale[rates==0] <- 1

plot(age, (ratesMale), xlab = xlabel, ylab = ylabel, col = colM, ylim = yMaxMin, xlim =range(c(0:16)), type = 'l', lwd =2, axes = FALSE)
lines(age, (ratesFemale), lwd =2, col = colF)
axis(2, tck = 1, col = "grey", lty = "dotted")
axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
box()

legend("topleft",inset = 0.01, c("Male", "Female"), col = c(colM, colF), lty = c(1, 1), lwd =2, bg = "white", )

}#End if else if


caption <- paste("Age-specific incidence log rates", period, sep = "")
caption <- paste(caption, " \n", sep = "")
caption <- paste(caption, site, sep = "")

title(caption, cex = 0.05)


}#End function plotAgeSpecIncRates