plotLogAgeSpecIncRates <- function(dataMaleRates, dataFemaleRates, site, nrOfAgeGroups, ageGrLabel, outFileTable, siteName, plotOnePage, plotTables, i, fileType){

xlabel <- "Age"
ylabel <- "Log rates per 100,000"

colM <- colors()[28]
colF <- colors()[554]

age<- seq(0, (nrOfAgeGroups - 1), 1)

	if(!is.data.frame(dataFemaleRates)){

	rates <- dataMaleRates$RATESper100000 
	rates[rates == 0] <- 10^(-10)
	rates <- round(log(rates), 4)

		if(plotTables){
		
			tableData <- cbind(dataMaleRates, rates)
			tableData$ICD10GROUPLABEL <- siteName
			colnames(tableData) <- c(colnames(dataMaleRates), "logRATES")
			makeTable(tableData, outFileTable, i, fileType)

		}else{

			yMaxMin <- range(c(0, max(rates)))
			period <- dataMaleRates$YEAR[1]

			plot(age, rates, xlab = xlabel, ylab = ylabel, col = colM, axes = FALSE, ylim = yMaxMin, lwd =1, type = 'n',  cex.lab = 0.9)
			##Produce segments of lines
			for(i in 1:(length(rates)-1)){
				if(rates[i] >0 & rates[i+1] >0){
					segments(age[i], rates[i], age[i+1], rates[i+1], col = colM, lwd = 2)
					}
			}
			axis(2, tck = 1, col = "grey", lty = "dotted")
			axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
			box()

			if((!plotOnePage) ){
				legend("topleft",inset = 0.01, "Male", col = colM, lty = 1, lwd = 1, bg = "white")
			}

		}

	}else if(!is.data.frame(dataMaleRates)){

		rates <- dataFemaleRates$RATESper100000 
		rates[rates == 0] <- 10^(-10)
		rates <- round(log(rates), 4)

	if(plotTables){

	tableData <- cbind(dataFemaleRates, rates)
	tableData$ICD10GROUPLABEL <-  siteName
	colnames(tableData) <- c(colnames(dataFemaleRates), "logRATES")
	makeTable(tableData, outFileTable, i, fileType)

	}else{

	yMaxMin <- range(c(0, max(rates)))
	period <- dataFemaleRates$YEAR[1]
	plot(age, rates, xlab = xlabel, ylab = ylabel, col = colF, axes = FALSE, ylim = yMaxMin, lwd = 1, type = 'n',  cex.lab = 0.9)

		for(i in 1:(length(rates)-1)){
				if(rates[i] >0 & rates[i+1] >0){
					segments(age[i], rates[i], age[i+1], rates[i+1], col = colF, lwd = 2)
					}
			}

	axis(2, tck = 1, col = "grey", lty = "dotted")
	axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
	box()

		if((!plotOnePage)){
			legend("topleft",inset = 0.01, "Female", col = colF, lty = 1, lwd =2, bg = "white")
		}

	}

}else {

	ratesFemale <- dataFemaleRates$RATESper100000 
	ratesFemale[ratesFemale == 0] <- 10^(-10)
	ratesFemale <- round(log(ratesFemale), 4)

	ratesMale <- dataMaleRates$RATESper100000 
	ratesMale[ratesMale == 0] <- 10^(-10)
	ratesMale <- round(log(ratesMale), 4)

	if(plotTables){
		##For males
		tableDataM <- cbind(dataMaleRates, ratesMale)
		tableDataM$ICD10GROUPLABEL <-  siteName
		colnames(tableDataM) <- c(colnames(dataMaleRates), "logRATES")
		makeTable(tableDataM, outFileTable, i, fileType)

		##For females
		tableDataF <- cbind(dataFemaleRates, ratesFemale)
		tableDataF$ICD10GROUPLABEL <- siteName
		colnames(tableDataF) <- c(colnames(dataFemaleRates), "logRATES")
		makeTable(tableDataF, outFileTable, i, fileType)
	}else{

		period <- dataMaleRates$YEAR[1]
		yMaxMin <- range(c(0, max(ratesMale), max(ratesFemale)))

		plot(age, ratesMale, xlab = xlabel, ylab = ylabel, col = colM, ylim = yMaxMin, type = 'n', lwd =1, axes = FALSE, cex.lab = 0.9)

		for(i in 1:(length(ratesMale)-1)){
			if(ratesMale[i] >0 & ratesMale[i+1] >0){
				segments(age[i], ratesMale[i], age[i+1], ratesMale[i+1], col = colM, lwd = 2)
			}
	}


	axis(2, tck = 1, col = "grey", lty = "dotted")
	axis(1, 0:(nrOfAgeGroups-1), ageGrLabel)
	box()
	lines(age, ratesFemale, lwd =1, col = colF, type = 'n')

	for(i in 1:(length(ratesFemale)-1)){
		if(ratesFemale[i] >0 & ratesFemale[i+1] >0){
			segments(age[i], ratesFemale[i], age[i+1], ratesFemale[i+1], col = colF, lwd = 2)
			}
	}

	if((!plotOnePage)){

	legend("topleft",inset = 0.01, c("Male", "Female"), col = c(colM, colF), lty = c(1, 1), lwd =2, bg = "white")

	}
}

}#End if else if

if(!plotTables){

	caption <- paste("Age-specific log incidence rates per 100,000 in ", period, sep = "")
	if(plotOnePage){
		if(makeTitleOnce){
			makeTitleOnce<<- FALSE
			title(caption, outer=TRUE)
			legend("topleft",inset = 0.01, c("M", "F"), col = c(colM, colF), lty = c(1, 1), lwd =2, bg = "white", cex = 0.6)
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
}##End function plotAgeSpecIncRates