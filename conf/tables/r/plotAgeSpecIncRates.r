plotAgeSpecIncRates <- function(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable, siteName, plotOnePage, plotTables, i, fileType){

##Labels
xlabel <- "Age"
ylabel <- "Rates per 100,000"

##Col for male and female
colM <- colors()[28]
colF <- colors()[554]

age<- seq(0, (nrOfAgeGroups - 1), 1)

if(!is.data.frame(dataFemaleRates)){

	dataMaleRates$ICD10GROUPLABEL <- siteName

		if(plotTables){
			makeTable(dataMaleRates, outFileTable, i, fileType)

		}else{

			yMaxMin <- range(c(0, dataMaleRates$RATESper100000))
			period <- dataMaleRates$YEAR[1]

			plot(age, dataMaleRates$RATESper100000, xlab = xlabel, ylab = ylabel, col = colM, axes = FALSE, ylim = yMaxMin, lwd =2, type = 'l')
			axis(2, tck = 1, col = "grey", lty = "dotted")
			axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
			box()
			
			if((!plotOnePage)){
				legend("topleft",inset = 0.01, "Male", col = colM, lty = 1, lwd =2, bg = "white")
			}
		}

}else if(!is.data.frame(dataMaleRates)){

	dataFemaleRates$ICD10GROUPLABEL <- siteName

	if(plotTables){
		makeTable(dataFemaleRates, outFileTable, i, fileType)

	}else{

		yMaxMin <- range(c(0, dataFemaleRates$RATESper100000))
		period <- dataFemaleRates$YEAR[1]
		
		plot(age, dataFemaleRates$RATESper100000, xlab = xlabel, ylab = ylabel, col = colF, axes = FALSE, ylim = yMaxMin, lwd =2, type = 'l')
		axis(2, tck = 1, col = "grey", lty = "dotted")
		axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
		box()

		if((!plotOnePage)){
			legend("topleft",inset = 0.01, "Female", col = colF, lty = 1, lwd =2, bg = "white")
		}

	}

}else {

	dataMaleRates$ICD10GROUPLABEL <- siteName
	dataFemaleRates$ICD10GROUPLABEL <- siteName

	if(plotTables){
		makeTable(dataMaleRates, outFileTable, i, fileType)

		makeTable(dataFemaleRates, outFileTable, i, fileType)

	}else{
		period <- dataMaleRates$YEAR[1]

		yMaxMin <- range(c(0, dataMaleRates$RATESper100000, dataFemaleRates$RATESper100000))

		plot(age, (dataMaleRates$RATESper100000), xlab = xlabel, ylab = ylabel, col = colM, ylim = yMaxMin, type = 'l', lwd =2, axes = FALSE)
		axis(2, tck = 1, col = "grey", lty = "dotted")
		axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
		box()
		lines(age, (dataFemaleRates$RATESper100000), lwd =2, col = colF)

		if((!plotOnePage)){
			legend("topleft",inset = 0.01, c("Male", "Female"), col = c(colM, colF), lty = c(1, 1), lwd =2, bg = "white")
		}
	}

}#End if else if

	if(!plotTables){

		caption <- paste("Age-specific incidence rates per 100,000 in ", period, sep = "")

		if(plotOnePage){
	
			if(makeTitleOnce){
				makeTitleOnce<<- FALSE

				title(caption, outer=TRUE)

				legend("topleft",inset = 0.01, c("M", "F"), col = c(colM, colF), lty = c(1, 1), lwd =2, bg = "white", cex = 1)
			}

			caption <- paste(siteName, " (", sep = "")
			caption <- paste(caption, site, sep = "")
			caption <- paste(caption, ")", sep = "")
			title(caption, cex.main = 0.9)

		}else if(!plotOnePage){

			caption <- paste(caption, " \n", sep = "")
			caption <- paste(caption, siteName, sep = "")
			caption <- paste(caption, " (", sep = "")
			caption <- paste(caption, site, sep = "")
			caption <- paste(caption, ")", sep = "")

			title(caption, cex = 0.9)

		}
}

}#End function plotAgeSpecIncRates