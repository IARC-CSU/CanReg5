plotAgeSpecIncRates <- function(dataMaleRates, dataFemaleRates){

xlabel <- "Age"
ylabel <- "Rates per 100,000"

colM <- colors()[28]
colF <- colors()[554]


if(!is.data.frame(dataFemaleRates)){

plot(dataMaleRates$AGE, dataMaleRates$RATES, xlab = xlabel, ylab = ylabel, col = colM)
legend("topleft",inset = 0.01, "Male", col = colM, lty = 1)


}else if(!is.data.frame(dataMaleRates)){

plot(dataFemaleRates$AGE, dataFemaleRates$RATES, xlab = xlabel, ylab = ylabel, col = colF)
legend("topleft",inset = 0.01, "Female", col = colF, lty = 1)


}else {

yMaxMin <- range(c(dataMaleRates$RATES, dataFemaleRates$RATES))

#print(yMaxMin)

plot(dataMaleRates$AGE, dataMaleRates$RATES, xlab = xlabel, ylab = ylabel, col = colM, ylim = yMaxMin, type = 'l')
lines(dataFemaleRates$AGE, dataFemaleRates$RATES, col = colF)
legend("topleft",inset = 0.01, c("Male", "Female"), col = c(colM, colF), lty = c(1, 1))


}#End if else if


}#End function plotAgeSpecIncRates