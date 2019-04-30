

shiny_data <- function(input, session) {
  
  dt_temp <- NULL
  if  (!is.null(input$select_table)) {
		table_number <- input$select_table
		if ( table_number == 1) {
			dt_temp <- canreg_pop_data(pop_file =ls_args$pop)
			
			
		}
		else if (table_number == 2){
			
			
			if (!is.null(input$radioSkin) & !is.null(input$radioAgeGroup)) {
				

				if (input$radioSkin == 1 ){
					bool_skin <- FALSE
				}
				else {
					bool_skin <- TRUE
				}
			
			if (input$radioAgeGroup == 1 ){
			temp <- (canreg_age_group$last_age)*5
			
					age_group = seq(5,temp,5)
				}
				else {
					age_group  <- c(15,30,50,70)
				}
				
			dt_temp  <- copy(dt_base)
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
			dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
			
			
			
			
			#dt_report <- canreg_age_cases_data(dt_report,age_group = c(5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80), skin=TRUE)
			
				
				dt_temp <- canreg_age_cases_data(dt_temp,age_group= age_group, skin=bool_skin)
				
				
			}
			
		}
		else if (table_number == 3){
			
			
			if (!is.null(input$radioSkin)) {
				

				if (input$radioSkin == 1 ){
					bool_skin <- FALSE
				}
				else {
					bool_skin <- TRUE
				}
			
		 
				
			dt_temp  <- copy(dt_base)
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
			dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
				
				dt_temp <- canreg_age_cases_data(dt_temp, skin=bool_skin)
				
			}
			
		}
		else if (table_number == 4){
			
			
			if (!is.null(input$slideAgeRange)& !is.null(input$radioValue)) {
		
				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				
				dt_temp <- canreg_ageSpecific_rate_data( dt_temp, keep_ref = TRUE)
				
				if (isolate(input$radioValue) == "cum") {
					if (last_age > 15) last_age <-15
					dt_temp <- csu_cum_risk_core(df_data =dt_temp,
																			 var_age="AGE_GROUP", var_cases="CASES", var_py="COUNT",
																			 group_by = c("cancer_label", "SEX"),
																			 missing_age = canreg_missing_age(dt_temp),
																			 age_label_list = "AGE_GROUP_LABEL",
																			 last_age= last_age)
				}
				else{
				
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																			group_by = c("cancer_label", "SEX"), missing_age = canreg_missing_age(dt_temp),
																			first_age = first_age,
																			last_age= last_age,
																			pop_base_count = "REFERENCE_COUNT",
																			age_label_list = "AGE_GROUP_LABEL")
				
				}
			
			}
			
		}
		else if (table_number == 5){
			
			
			if (!is.null(input$slideAgeRange) & !is.null(input$radioValue)) {
		
				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
			
				dt_temp <- canreg_ageSpecific_rate_data( dt_temp, keep_ref = TRUE)
				
				if (isolate(input$radioValue) == "cum") {
					if (last_age > 15) last_age <-15
					dt_temp <- csu_cum_risk_core(df_data =dt_temp,
																			 var_age="AGE_GROUP", var_cases="CASES", var_py="COUNT",
																			 group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"),
																			 missing_age = canreg_missing_age(dt_temp),
																			 age_label_list = "AGE_GROUP_LABEL",
																			 last_age= last_age)
				}
				else{
				
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																			group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																			first_age = first_age,
																			last_age= last_age,
																			pop_base_count = "REFERENCE_COUNT",
																			age_label_list = "AGE_GROUP_LABEL")
				
				}
			
			}
			
		}
		else if (table_number == 6){

			dt_temp  <- copy(dt_base)
			dt_temp <- dt_temp[ICD10GROUP != "C44",]
			dt_temp <- dt_temp[ICD10GROUP != "O&U",]
			dt_temp <- canreg_ageSpecific_rate_data(dt_temp)
				
		}
		else if (table_number == 7){

			dt_temp  <- copy(dt_base)
			dt_temp <- dt_temp[ICD10GROUP != "C44",]
			dt_temp <- dt_temp[ICD10GROUP != "O&U",]
			dt_temp <- canreg_ageSpecific_rate_data(dt_temp)
				
		}
		else if (table_number == 8){

			if (!is.null(input$radioSkin)) {
				

				if (input$radioSkin == 1 ){
					bool_skin <- FALSE
				}
				else {
					bool_skin <- TRUE
				}

				dt_temp  <- copy(dt_base)
				dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
				dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
				dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
					
				dt_temp <- canreg_year_cases_data(dt_temp, skin=bool_skin)
				
			}
				
		}
		else if (table_number == 9){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE, keep_year = TRUE)
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																				group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																				first_age = first_age,
																				last_age= last_age,
																				pop_base_count = "REFERENCE_COUNT",
																				age_label_list = "AGE_GROUP_LABEL") 																 
			}
				
		}
		else if (table_number == 10){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE, keep_year = TRUE)
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																				group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																				first_age = first_age,
																				last_age= last_age,
																				pop_base_count = "REFERENCE_COUNT",
																				age_label_list = "AGE_GROUP_LABEL")

																	
			}
				
		}
		else if (table_number == 11){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE, keep_year = TRUE)
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																				group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																				first_age = first_age,
																				last_age= last_age,
																				pop_base_count = "REFERENCE_COUNT",
																				age_label_list = "AGE_GROUP_LABEL")  
																				
				dt_temp <- as.data.table(dt_temp)
			}
				
		}
		else if (table_number == 12){

			if (!is.null(input$selectRegistry1) & 
				!is.null(input$selectRegistry2) & 
				!is.null(input$selectRegistry3) ) {

				registry_selection <- 
					c(
					input$selectRegistry1,
					input$selectRegistry2,
					input$selectRegistry3,
					ls_args$header)  

				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE)

				dt_CI5_data <- canreg_import_CI5_data(dt_temp, paste0(script.basename, "/CI5_alldata.rds"))
				dt_temp <- shiny_merge_CI5_registry(dt_temp,dt_CI5_data, registry_region = ls_args$sr, registry_label = ls_args$header, number=40 )
				
				dt_temp <- dt_temp[country_label %in% registry_selection, ]
				setkeyv(dt_temp, c("CSU_RANK", "SEX","asr"))
				dt_temp[country_label!=ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
				dt_temp[country_label==ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
				cancer_list <- sort(as.character(unique(dt_temp$cancer_label)))
				cancer_selected <- as.character(unique(dt_temp[CSU_RANK == 1 & SEX == levels(SEX)[1],]$cancer_label))
				
				updateSelectInput(session, "selectCancerSite",choices =cancer_list,selected = cancer_selected)



			}
		}	
	}
 
  return(dt_temp)
  
}




shiny_plot <- function(dt_plot,input,session, download = FALSE,slide=FALSE, file = NULL) {
  
	if  (!is.null(input$select_table)) {
  
		if (download) {
			table_number <- input$select_table
			if (slide) {
				ls_args$header  <- ""
				output_type <- "png"
			}
    		else {
      			output_type <- input$select_format
    		}
  		}
		else {
			table_number <- isolate(input$select_table)
			if (table_number != 12) {
				ls_args$header  <- ""
			}
			
		}
		
	
		
		if ( table_number == 1) {
			
			if (download) {

				canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
											FUN=canreg_population_pyramid,
											df_data=dt_plot,
											canreg_header = ls_args$header)
			}
			else {
				canreg_population_pyramid( df_data=dt_plot, canreg_header = ls_args$header)
			}
		
			
		}
		else if (table_number == 2){
			
			if (isolate(input$radioSkin) == 1 ){
				bool_skin  <- FALSE
			}
			else {
				bool_skin  <- TRUE
			}
			
			if (download) {
				canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
											FUN=canreg_cases_age_bar,
											df_data=dt_plot,
											color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
											canreg_header = ls_args$header,
											skin=bool_skin)
			}
			else {
				canreg_cases_age_bar(
					df_data=dt_plot,
					color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
					canreg_header = ls_args$header,
					skin=bool_skin
				)
			}
			
		}
		else if (table_number == 3){
			
			if (isolate(input$radioSkin) == 1 ){
			header = paste0(ls_args$header, "\n\n",i18n$t("All cancers but C44"))
			}
			else {
			header = paste0(ls_args$header, "\n\n",i18n$t("All cancers"))
			}
			
			if (download) {
			canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
									FUN=canreg_age_cases_pie_multi_plot,
									dt=dt_plot,
									canreg_header = header)
			}
			else {
				canreg_age_cases_pie_multi_plot(
					dt=dt_plot,
					canreg_header = header)
				
			}
			
		}
		else if (table_number == 4){
		
			if (!is.null( input$slideNbTopBar)& !is.null(input$radioValue)) {
			
				nb_top <- input$slideNbTopBar
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 
				

				
				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				
				if (isolate(input$radioValue) == "asr") {
					var_top <- "asr"
					digit <- 1
					ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ", i18n$t("years old"))
					
					
				} 
				else if (isolate(input$radioValue) == "cases"){
					var_top <- "CASES"
					digit <- 0
					ytitle <-  paste0(i18n$t("Number of cases"),", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
					
					
				}
				else if (isolate(input$radioValue) == "cum") {
					var_top <- "cum_risk"
					digit <- 2
					if (last_age >= 15) {
						age2 <- 74
					} else {
						age2 <- isolate(input$slideAgeRange)[2]-1
					}
					ytitle<-paste0(i18n$t("Cumulative incidence risk (percent)"),", 0-",age2, " ",i18n$t("years old"))
					
					
				}
				
				if (download) {
					canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
												FUN=canreg_bar_top,
												df_data=dt_plot,
												var_top = var_top,
												color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
												nb_top = nb_top,nsmall=digit,
												canreg_header  = ls_args$header,
												ytitle=ytitle)
				}
				else {
					canreg_bar_top(df_data=dt_plot,
												 var_top = var_top,
												 color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
												 nb_top = nb_top,nsmall=digit,
												 canreg_header = ls_args$header,
												 ytitle=ytitle)
				}
			 
				
				
				
				
			}
			
		}
		
		else if (table_number == 5){
		
			if (!is.null( input$slideNbTopBar)& !is.null(input$radioValue)) {
			
				nb_top <- input$slideNbTopBar
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1

				
				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				
				if (isolate(input$radioValue) == "asr") {
					var_top <- "asr"
					digit <- 1
					ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
					
					
				} 
				else if (isolate(input$radioValue) == "cases"){
					var_top <- "CASES"
					digit <- 0
					ytitle <-  paste0(i18n$t("Number of cases"),", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
					
					
				}
				else if (isolate(input$radioValue) == "cum") {
					var_top <- "cum_risk"
					digit <- 2
					if (last_age >= 15) {
						age2 <- 74
					} else {
						age2 <- isolate(input$slideAgeRange)[2]-1
					}
					ytitle<-paste0(i18n$t("Cumulative incidence risk (percent)"),", 0-",age2, " ",i18n$t("years old"))
					
					
				}
				
				if (download) {
										
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = TRUE,
												FUN=canreg_bar_top_single,
												dt=dt_plot,
												var_top = var_top,
												nb_top = nb_top,digit=digit,
												canreg_header  = ls_args$header,
												xtitle=ytitle)
				}
				else {
					 temp <- canreg_bar_top_single(
						dt=dt_plot,
						var_top = var_top,
						nb_top = nb_top,digit=digit,
						canreg_header  = ls_args$header,
						xtitle=ytitle,
						return_plot=TRUE)
						
						

			
					grid.arrange(temp$plotlist[[1]], temp$plotlist[[2]], ncol=2)
						
					
						
						
						
				}
			 
				
				
				
				
			}
			
		}
		else if (table_number == 6){
		
			if (!is.null( input$slideNbTopBar) & !is.null(input$radioLog)) {
			
				nb_top <- input$slideNbTopBar
				
				if (input$radioLog == "log") {
					bool_log <- TRUE
				}
				else {
					bool_log <- FALSE
				}

				if (download) {
					
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = TRUE,
								FUN=canreg_ageSpecific_rate_top,
								df_data=dt_plot,logscale = bool_log,nb_top = nb_top,
								plot_title = ls_args$header
								)
							
				}
				else {
					temp <- Rcan:::core.csu_ageSpecific_top(
						df_data=dt_plot,
						var_age="AGE_GROUP",
						var_cases= "CASES", 
						var_py= "COUNT",
						var_top = "cancer_label",
						var_age_label_list = "AGE_GROUP_LABEL",
						group_by="SEX",
						missing_age=canreg_missing_age(dt_plot),
						var_color="ICD10GROUPCOLOR",
						logscale = bool_log,
						nb_top = nb_top,
						plot_title = ls_args$header,
						xtitle = i18n$t("Age at diagnosis"),
						ytitle = i18n$t("Age-specific incidence rate per")
					)
							
							
					temp$plotlist[[1]] <- temp$plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=2,byrow=TRUE))
					temp$plotlist[[2]] <- temp$plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=2,byrow=TRUE))
					grid.arrange(temp$plotlist[[1]], temp$plotlist[[2]], ncol=2)

				}
			 
				
				
				
				
			}
			
		}
		else if (table_number == 7){
			
			if (!is.null( input$selectCancerSite) & !is.null(input$radioLog)) {
			
				bool_log <- (input$radioLog == "log")
				color_trend <- c("Male" = "#2c7bb6", "Female" = "#b62ca1")
				dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
				
				 if (download) {
				 
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = FALSE,
								FUN=canreg_ageSpecific,
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_subtitle = isolate(input$selectCancerSite),
								color_trend = color_trend
								)
							
				}
				else {
					
					
					canreg_ageSpecific(
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_subtitle = isolate(input$selectCancerSite),
								color_trend = color_trend
								)
								
				}
				
			
			
			}
		
		}
		else if (table_number == 8){
			
			if (isolate(input$radioSkin) == 1 ){
				bool_skin  <- FALSE
			}
			else {
				bool_skin  <- TRUE
			}
			
			if (download) {
				canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
											FUN=canreg_cases_year_bar,
											dt=dt_plot,
											canreg_header = ls_args$header,
											skin=bool_skin)
			}
			else {
				 canreg_cases_year_bar(
					dt=dt_plot,
					canreg_header = ls_args$header,
					skin=bool_skin
				)
			}
			
		}
		else if (table_number == 9){

		
		
		 if ( !is.null( input$slideNbTopBar) & !is.null(input$radioLog) ) {

		 	
				
				if (input$radioLog == "log") {
					bool_log <- TRUE
				}
				else {
					bool_log <- FALSE
				}


				
				nb_top <- input$slideNbTopBar
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				

				 ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
		 
				if (download) {
				
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = TRUE,
								FUN=canreg_asr_trend_top,
								dt=dt_plot,number = nb_top,
								canreg_header = ls_args$header,
								logscale = bool_log,
								ytitle=ytitle)

				}
				else {
					temp <- canreg_asr_trend_top(
						dt=dt_plot,number = nb_top,
						canreg_header = ls_args$header,
						logscale = bool_log,
						return_plot=TRUE,
						ytitle=ytitle)
						
						
				grid.arrange(
					temp$plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)),
					temp$plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)),
					ncol=2)

				}
			}
				
			
		}
		else if (table_number == 10){
			
		 if (!is.null( input$slideNbTopBar) & !is.null(input$checkCI)) {
				
				bool_CI <- input$checkCI
				nb_top <- input$slideNbTopBar
				
				if (bool_CI) {
					group_by <- "SEX"
					graph_list <- TRUE
				} else{
					group_by <- NULL
					graph_list <- FALSE
				}
				
				dt_plot <- Rcan:::core.csu_dt_rank(dt_plot,
									var_value= "CASES",
									var_rank = "cancer_label",
									group_by = group_by,
									number = nb_top)
									
				dt_plot <- Rcan:::core.csu_eapc(dt_plot, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX",  "CSU_RANK"))
				dt_plot <-as.data.table(dt_plot)		
				
				
				
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				 ytitle <- paste0(i18n$t("Estimated annual percentage change")," (%), ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
				 color_bar <- c("Male" = "#2c7bb6", "Female" = "#b62ca1")
		 

				if (download) {
				
					if (bool_CI) {
					
							canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = graph_list,
									FUN=canreg_eapc_scatter_error_bar,
									dt=dt_plot,
									canreg_header = ls_args$header,
									ytitle=ytitle)
					
					
					
					}
					else {
						canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = graph_list,
									FUN=canreg_eapc_scatter,
									dt_plot=dt_plot,color_bar=color_bar,
									canreg_header = ls_args$header,
									ytitle=ytitle)
					}

				}
				else {
					if (bool_CI) {
					
						temp <- canreg_eapc_scatter_error_bar(
								dt=dt_plot,
								canreg_header = ls_args$header,
								return_plot = TRUE, 
								ytitle=ytitle)
								
						grid.arrange(temp$plotlist[[1]], temp$plotlist[[2]], ncol=2)
					
					}
					else {
					
						canreg_eapc_scatter(
							dt_plot=dt_plot,color_bar=color_bar,
							canreg_header = ls_args$header,
							ytitle=ytitle)
					}
				}
			}
				
			
		}
		else if (table_number == 11){
			
			if (!is.null( input$selectCancerSite) & !is.null(input$radioLog)) {
			
				bool_log <- (input$radioLog == "log")
				dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
				
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old") )
		 
				
				if (download) {
				 
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = FALSE,
								FUN=canreg_asr_trend,
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_title=ls_args$header,
								ytitle = ytitle
								)
							
				}
				else {
					
					
					canreg_asr_trend(
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_title=ls_args$header,
								ytitle = ytitle
								)
								
				}

			}
		
		}
		else if (table_number == 12){
			
			if (!is.null( input$selectCancerSite) & !is.null(input$radioSex)) {

				dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
				dt_plot <- dt_plot[SEX == input$radioSex]

				if (nrow(dt_plot) == 0 ) {
					temp <- ifelse(input$radioSex == "Male", "Female", "Male")
					updateRadioButtons(session, "radioSex", selected= temp)
					return()
				}

				xtitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","))

				
				if (download) {
				 
					canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
								FUN=shiny_bar_CI5_compare,
								dt=dt_plot,
								xtitle = xtitle,
		                		text_size_factor=1.1)
							
				}
				else {
					
					shiny_bar_CI5_compare(
						dt=dt_plot,
						xtitle = xtitle,
                		text_size_factor=1.2)
								
				}
			}
		
		}

  }
  
}



#create a new function to print the graph and return data (adapt to canreg_output function)
canreg_ageSpecific <- function(dt_plot,color_trend,plot_subtitle="",logscale=FALSE, landscape = TRUE,list_graph = FALSE, return_data = FALSE) {
	
	if (return_data) {
		
		dt_temp <- dt_plot[COUNT > 0,]
		dt_temp[, rate := CASES/COUNT*10000]
		dt_temp[, AGE_GROUP_LABEL := paste0("'",AGE_GROUP_LABEL,"'")]
		dt_temp <- dt_temp[, c("cancer_label",
								"AGE_GROUP",
								"AGE_GROUP_LABEL",
								"SEX",
								"CASES",
								"COUNT",
								"rate"), with=FALSE]
		setkeyv(dt_temp, c("SEX","AGE_GROUP"))
		return(dt_temp)
		stop() 
		
	} 
		
	plot<- Rcan:::core.csu_ageSpecific(dt_plot,
		var_age        ="AGE_GROUP",
		var_cases      = "CASES",
		var_py         ="COUNT",
		group_by       = "SEX",
		plot_title     = ls_args$header,
		plot_subtitle  = plot_subtitle,
		color_trend    = color_trend,
		logscale       = logscale,
		age_label_list = unique(dt_plot[["AGE_GROUP_LABEL"]]),
		xtitle         = i18n$t("Age at diagnosis"),
		ytitle         = i18n$t("Age-specific incidence rate per"),
		label_group_by = c(i18n$t("Male"),i18n$t("Female"))
 		 )$csu_plot
		
	print(plot)

}
 
 
canreg_asr_trend <- function(dt_plot,
								var_asr="asr", 
                                var_cases= "CASES", 
                                var_year= "YEAR",
                                group_by="SEX",
                                logscale = TRUE,
                                ytitle=NULL,
                                landscape = FALSE,
                                list_graph = FALSE,
                                return_data = FALSE,
								return_plot= FALSE,
                                plot_title="") 

{

  if (return_data) {
    dt <- dt[, c(group_by,var_year,var_asr), with=FALSE]
    setkeyv(dt, c("SEX",var_year))
    return(dt)
    stop() 
  }


   temp_level <- c(i18n$t(levels(dt_plot$SEX)[1]),i18n$t(levels(dt_plot$SEX)[2]))
   dt_plot$SEX <- factor(dt_plot$SEX,labels = temp_level) 

   color_trend <- c("#2c7bb6", "#b62ca1")
    
   plot <- Rcan:::core.csu_time_trend(dt_plot,
                                    var_trend = "asr",
                                    var_year = "YEAR",
                                    group_by = "SEX",
                                    logscale = logscale,
                                    smoothing = NULL,
                                    ytitle = ytitle,
									xtitle = i18n$t("Year"),
                                    plot_title = plot_title,
                                    color_trend = color_trend)$csu_plot


  print(plot)

}

#shiny function for CI5 comparison


shiny_bar_CI5_compare <- function(dt,group_by = "SEX", landscape = TRUE,list_graph=FALSE,
                                        xtitle = "",digit  =  1,text_size_factor =1.5,
                                        return_data  =  FALSE) 
{
  
  if (return_data) {
    setnames(dt, "CSU_RANK","cancer_rank")
    dt <-  dt[,-c("ICD10GROUPCOLOR"), with=FALSE]
    
    return(dt)
    stop() 
  }
  
  
  CI5_registries <- as.character(unique(dt$country_label))
  caption <- NULL
  if (any(grepl("\\*",CI5_registries))) {
    caption <- paste0("*: ",i18n$t("Regional registries"))
  }

  dt[["country_label"]] <-Rcan:::core.csu_legend_wrapper(dt[["country_label"]], 14)
  dt[,country_label:=factor(country_label, levels=country_label)]
      
  temp <- csu_bar_plot(dt=dt, 
                 var_top="asr",
                 var_bar="country_label",
                 plot_title = unique(dt$cancer_label),
                 plot_subtitle = unique(i18n$t(dt$SEX)), 
                 plot_caption = caption,
                 xtitle=xtitle,
                 digit = digit,
                 color_bar = as.character(dt$ICD10GROUPCOLOR),
                 text_size_factor = text_size_factor,
                 landscape = TRUE) 
 


      
  geom_text_index <- which(sapply(temp$layers, function(x) class(x$geom)[1]) == "GeomText")
  temp$layers[[geom_text_index]]$aes_params$size <- 6 
  geom_hline_index <- which(sapply(temp$layers, function(x) class(x$geom)[1]) == "GeomHline")
  temp$layers[[geom_hline_index]]$aes_params$size <- 0.4
      
  print(temp)
  
}


shiny_merge_CI5_registry <- function(dt, dt_CI5, registry_region, registry_label, number=5) {
  
  ##calcul of ASR for canreg
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX","ICD10GROUP","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt),
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  ##keep top 5 cancer for men and top 5 cancer women of canreg.
  dt <- Rcan:::core.csu_dt_rank(dt,var_value = "CASES",var_rank = "cancer_label",
                    group_by = "SEX", number =number, ties.method = "first") 
  
  #Keep selected cancer in CI5 data and prepare CI5 data
  
  dt_temp <- dt[,c("SEX", "ICD10GROUP", "CSU_RANK"),  with=FALSE]
  dt_CI5 <- merge(dt_CI5,dt_temp, by=c("SEX", "ICD10GROUP"),all.y=TRUE )
  dt_CI5 <- Rcan:::core.csu_asr(df_data =dt_CI5, var_age ="AGE_GROUP",var_cases = "cases", var_py = "py",
                         group_by = c("country_label","cr" ,"SEX","ICD10GROUP", "CSU_RANK"),
                         var_age_group=c("country_label"),
                         missing_age = canreg_missing_age(dt_CI5),
                         pop_base_count = "REFERENCE_COUNT",
                         age_label_list = "AGE_GROUP_LABEL")
  
  dt_CI5 <- as.data.table(dt_CI5)
  
  #keep CI5 selected region
  dt_CI5[, cr:=NULL]
  
  #add CI5 data to canreg data
  dt_temp <- unique(dt[,c("ICD10GROUP", "ICD10GROUPCOLOR", "cancer_label"),  with=FALSE])
  dt_CI5 <- merge(dt_CI5,dt_temp, by=c("ICD10GROUP"),all.x=TRUE )
  setnames(dt_CI5, "cases", "CASES")
  setnames(dt_CI5, "py", "COUNT")
  dt[, country_label:=registry_label]
  dt <- rbind(dt,dt_CI5)
  
  return(dt)
  
  
}

										
#function for multiple file download output_type
multiple_output <- function(table_number, bool_ci, output_format) {
	
	bool_temp <- FALSE
	if (output_format %in% c("png", "tiff", "svg")) {
		if (table_number %in% c(5,6,9)) {
			bool_temp <- TRUE
		}
		else if (table_number == 10 & bool_ci) {
			bool_temp <- TRUE
		}
	}
	
	return(bool_temp)

}                                        
										

shiny_error_log <- function(log_file,filename) {
  
  shiny_log <- file(log_file,open="wt")
  sink(shiny_log)
  sink(shiny_log, type="message")
  
  #print error
  cat("This file contains the data and parameter of this canreg5 R-shiny application.\n") 
  cat(paste0("If an error occured, please restart this application send this log file: `",filename,"` to canreg@iarc.fr, with a description of the error\n"))
  cat("The second part of this log (After '----------------------') contains your aggregated data, if you do not won't to share the aggregated data, you can delete this part.\n\n")
  cat("\n")
  
  #print argument from canreg
  print(ls_args)
  cat("\n")
  
  #print environment
  ##print(ls.str())
  ##cat("\n")
	
	#print R version and package load
  print(sessionInfo())
  cat("\n")

  #print missing package
  packages_list <- c("Rcpp", "data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png")

  missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]  
  if (length(missing_packages) == 0) {
    print("No missing package")
  } else {
    print(missing_packages)
  }
  cat("\n")
  
  #test loading package
  lapply(packages_list, require, character.only = TRUE)
  cat("\n")
  
  print("----------------------")
  cat("\n")
  #print incidence / population file (r format)
  cat("Incidence file\n")
  dput(read.table(ls_args$inc, header=TRUE, sep="\t"))
  cat("\n")
  cat("population file\n")
  dput(read.table(ls_args$pop, header=TRUE, sep="\t"))
  cat("\n")
	
	#close log_file and send to canreg
  sink(type="message")
  sink()
  close(shiny_log)
  
}

shiny_dwn_data <- function(log_file) {

	dt_temp <- copy(dt_base)
	dt_temp[, ICD10GROUPLABEL := NULL]
	dt_temp[, ICD10GROUPCOLOR := NULL]
	dt_temp[, AGE_GROUP := NULL]
	
	write.csv(dt_temp, paste0(log_file),row.names = FALSE)

}

shiny_dwn_report <- function(log_file, progress_bar) {


  	ls_args$out <- tempdir()

  	temp <- min(as.numeric(substr(ls_args$agegroup,regexpr("-", ls_args$agegroup)[1]+1,nchar(ls_args$agegroup))),14)
  	canreg_age_group_cr <- canreg_get_agegroup_label(dt_base, paste0("0-",temp))



	#check if report path exist (if not create report path)
	pos <- regexpr("\\\\[^\\\\]*$", ls_args$out)[[1]]
	path <- substr(ls_args$out,start=1, stop=pos)
	if (ls_args$sc=="null") {
	report_path <- paste0(path, "report-template")
	} else {
	report_path <- paste0(path, "report-template-", ls_args$sc)
	}
	if(!file_test("-d",report_path)) {
	dir.create(report_path)
	}

	incProgress(0, detail = "create docx")

    list_number <- list(fig=1, tbl=1, example=FALSE)
    doc <- read_docx(paste(sep="/", script.basename,"slide_template", "template.docx"))
  
	doc <- body_add_par( doc, value = ls_args$header, style="TitleDoc")
	#oups need solution to add real title
	doc <- body_add_par(doc, "\r\n")
	doc <- body_add_par(doc, "\r\n")
	doc <- body_add_toc(doc)

	incProgress(1/10, detail = "import / create template")

	dt_chapter <- canreg_report_template_extract(report_path, script.basename)
  
	if (year_info$span <= time_limit) {
		dt_chapter <- dt_chapter[title != "Estimated annual percentage change"]
	}

	list_number <- canreg_report_chapter_txt(dt_chapter, doc, report_path,dt_base,pop_file =ls_args$pop,list_number)


	doc <- body_add_break(doc)
	doc <- body_add_par(doc, "Results", style =  paste("heading",1))

	
	

	# bar chart age
	dt_report <- dt_base
	dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
	dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
	dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
	dt_report <- canreg_age_cases_data(dt_report,age_group = c(5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80), skin=TRUE)

	total_cases <- formatC(dt_report[,sum(CASES)], format="d", big.mark=",")
	total_male <- formatC(dt_report[SEX == "Male",sum(CASES)], format="d", big.mark=",")
	total_female <-formatC(dt_report[SEX == "Female",sum(CASES)], format="d", big.mark=",")

	if (year_info$max == year_info$min) {
		text_year <- paste0("In ",year_info$max,", ")
	} else {
		text_year <- paste0("Between ",year_info$min," and ",year_info$max, ", ")
	}

  doc <- body_add_par(doc,paste0(text_year,total_cases," cases of cancers were registered: ",
                                 total_male," among men and ",total_female," among women."))
  
  doc <- body_add_par(doc, "Number of cases in period, by age group & sex", style = "heading 2")
  incProgress(1/10, detail = "Number of cases in period, by age group & sex")
  
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph_a",list_number$fig ),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_cases_age_bar,
                df_data =dt_report,
                canreg_header = "", skin=FALSE)
  
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,"a. Bar chart, distribution of cases by age group and sex"))
  
  dt_report <- dt_base
  dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
  dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
  dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
  dt_report <- canreg_age_cases_data(dt_base, skin=TRUE)
  
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph_b", list_number$fig),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_age_cases_pie_multi_plot,
                dt=dt_report,
                canreg_header = i18n$t("All cancers but C44"))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc,paste0("Fig ",list_number$fig,"b. Pie chart, distribution of cases by age group and sex"))
  list_number$fig <- list_number$fig +1
  
  doc <- body_add_par(doc, "Number of cases by year", style = "heading 2")
  incProgress(1/10, detail = "Number of cases by year")

  
  dt_report <- dt_base
  dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U" 
  dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified" 
  dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
  
  dt_report <- canreg_year_cases_data(dt_report, skin=FALSE)
  
  
  ##Produce output
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_cases_year_bar,
                dt=dt_report,
                canreg_header = "", skin=FALSE)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc,paste0("Fig ",list_number$fig,". Number of cases by year"))
  list_number$fig <- list_number$fig +1
  
  
  
  doc <- body_add_par(doc, "The most common cancers, by sex",style = "heading 2")
  incProgress(1/10, detail = "The most common cancers, by sex")
  
  dt_report <- dt_base
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  
  dt_report <- canreg_ageSpecific_rate_data(dt_report, keep_ref = TRUE)
  
  
  
  
  dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                  group_by = c("cancer_label", "SEX"),
                                  missing_age = canreg_missing_age(dt_report),
                                  first_age = canreg_age_group$first_age+1,
                                  last_age= canreg_age_group$last_age+1,
                                  pop_base_count = "REFERENCE_COUNT",
                                  age_label_list = "AGE_GROUP_LABEL")
  
  
  
  text_male <- canreg_report_top_cancer_text(dt_report, 5, sex_select="Male")
  text_female <- canreg_report_top_cancer_text(dt_report, 5, sex_select="Female")
  
  doc <- body_add_par(doc, paste0("In men, ",tolower(text_male)))
  doc <- body_add_par(doc, paste0("In women, ",tolower(text_female)))
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph_a", list_number$fig),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_bar_top,
                df_data=dt_report,
                var_top="CASES",
                nsmall=0,
                color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
                canreg_header = "",
                ytitle=paste0(i18n$t("Number of cases"),", ", canreg_age_group$label))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,"a. Top 10 cancers, both sexes (Number of cases)"))
  
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph_b", list_number$fig),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_bar_top,
                df_data=dt_report,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
                canreg_header = "",
                ytitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,"b. Top 10 cancers, both sexes (Age-standardized rate per 100,000)"))
  list_number$fig=list_number$fig+1
  
  doc <- body_add_par(doc, "\r\n")
  
  
  
  dt_report <- dt_base
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  dt_report <- canreg_ageSpecific_rate_data(dt_base, keep_ref = TRUE, keep_year = FALSE)
  
  
  ##calcul of ASR
  dt_asr <- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_base),
                                first_age = canreg_age_group$first_age+1,
                                last_age= canreg_age_group$last_age+1,
                                pop_base_count = "REFERENCE_COUNT",
                                age_label_list = "AGE_GROUP_LABEL")
  
  ##calcul of cumulative risk
  dt_cum_risk <- csu_cum_risk_core(df_data = dt_report,var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                   group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_base),
                                   last_age= canreg_age_group_cr$last_age+1,
                                   age_label_list = "AGE_GROUP_LABEL")
  
  dt_bar <- dt_asr
  var_top <- "CASES"
  digit <- 0
  xtitle <- paste0(i18n$t("Number of cases"),", ", canreg_age_group$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- compose(ft,
               i = 1, j = 1,
               value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- compose(ft,
                i = 1, j = 2,
                value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Top 10 cancers, number of cases"))
  list_number$fig = list_number$fig+1
  
  
  var_top <- "asr"
  digit <- 1
  xtitle<-paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- compose(ft,
               i = 1, j = 1,
               value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- compose(ft,
                i = 1, j = 2,
                value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Top 10 cancers, ASR"))
  list_number$fig <- list_number$fig+1
  
  var_top <- "cum_risk"
  digit <- 2
  dt_bar <- dt_cum_risk
  xtitle<-paste0(i18n$t("Cumulative incidence risk (percent)"),", ", canreg_age_group_cr$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- compose(ft,
               i = 1, j = 1,
               value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- compose(ft,
                i = 1, j = 2,
                value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Top 10 cancers, cumulative risk, 0-74 years"))
  list_number$fig <- list_number$fig+1
  
  doc <- body_add_break(doc)
  
  doc <- body_add_par(doc, "Age-specific incidence rates (most common sites) by sex", style = "heading 2")
  incProgress(1/10, detail = "Age-specific incidence rates (most common sites) by sex")
  
  
  dt_report <- dt_base
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  dt_report <- canreg_ageSpecific_rate_data(dt_report)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_ageSpecific_rate_top,
                df_data=dt_report,logscale = TRUE,nb_top = 5,
                plot_title = "")
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- compose(ft,
               i = 1, j = 1,
               value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- compose(ft,
                i = 1, j = 2,
                value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Age-specific incidence rates"))
  list_number$fig <- list_number$fig+1
  
  
  if (year_info$span >  time_limit) {
    
    doc <- body_add_break(doc)
    doc <- body_add_par(doc, "Trend in ASR (most common sites) by sex", style = "heading 2")
    incProgress(1/10, detail = "Trend in ASR (most common sites) by sex")

    
    dt_report <- canreg_ageSpecific_rate_data(dt_base, keep_ref = TRUE, keep_year = TRUE)
    
    ##calcul of ASR
    dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_base),
                                    first_age =canreg_age_group$first_age+1,
                                    last_age= canreg_age_group$last_age+1,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
    
    
    #produce graph
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                  FUN=canreg_asr_trend_top,
                  dt=dt_report,number = 5,
                  canreg_header = "",
                  ytitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
    
    temp <- data.frame(1,2)
    ft <- flextable(temp)
    ft <- width(ft, width = dim_width)
    ft <- compose(ft,
                 i = 1, j = 1,
                 value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
    
    ft <- compose(ft,
                  i = 1, j = 2,
                  value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
    
    ft <- delete_part(ft, part = "header")
    ft <- border(ft,  border=fp_border(width=0))
    
    doc <- body_add_par(doc, "\r\n")
    doc <- body_add_flextable(doc,ft, align = "center")
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Trend in Age-standardized (W) incidence rate"))
    list_number$fig <- list_number$fig+1
    
  }
  
  if (year_info$span >  time_limit) {
    
    doc <- body_add_break(doc)
    doc <- body_add_par(doc, i18n$t("Estimated annual percentage change"), style = "heading 2")
    incProgress(1/10, detail = "Estimated annual percentage change")

    
    dt_report <- canreg_ageSpecific_rate_data(dt_base, keep_ref = TRUE, keep_year = TRUE)
    
    
    ##calcul of ASR
    dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX", "YEAR"), missing_age = canreg_missing_age(dt_base),
                                    first_age = canreg_age_group$first_age+1,
                                    last_age= canreg_age_group$last_age+1,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
    
    ##Keep top based on rank
    dt_report <- Rcan:::core.csu_dt_rank(dt_report,
                                         var_value= "CASES",
                                         var_rank = "cancer_label",
                                         group_by = "SEX",
                                         number = 25
    )
    
    
    ##calcul eapc
    dt_report <- Rcan:::core.csu_eapc(dt_report, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX","CSU_RANK"))
    dt_report <-as.data.table(dt_report)
    
    
    #produce graph
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = TRUE,list_graph = TRUE,
                  FUN=canreg_eapc_scatter_error_bar,
                  dt=dt_report,
                  canreg_header = i18n$t("Estimated Average Percentage Change"),
                  ytitle=paste0(i18n$t("Estimated average percentage change")," (%), ", canreg_age_group$label))
    
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
    doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"),width=graph_width*0.9,height=graph_width*0.9*dims[1]/dims[2],style="centered" )
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Estimated annual percentage change, male"))
    doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"),width=graph_width*0.9,height=graph_width*0.9*dims[1]/dims[2],style="centered" )
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Estimated annual percentage change, female"))
    list_number$fig=list_number$fig+1 
    doc <- body_add_par(doc, "\r\n")
    
  }
  
  
  
  ## comparison with CI5 registries.
  
  region_admit <- c("EastMed", "Americas", "West Pacific", "Europe", "SEAsia", "Africa")
  
  if (ls_args$sr %in% region_admit) {
    
    doc <- body_add_break(doc)
    doc <- body_add_par(doc, "Comparison of summary rates with other registries (in same region)", style = "heading 2")
    incProgress(1/10, detail = "Comparison of summary rates with other registries (in same region)")

    
    doc <- body_add_par(doc, "\r\n")
    
    
    dt_report <- canreg_ageSpecific_rate_data(dt_base, keep_ref = TRUE)
    
    # import CI5 data with same cancer code and same age group
    dt_CI5_data <- canreg_import_CI5_data(dt_report, paste0(script.basename, "/CI5_data.rds"))
    
    
    
    #merge CI5 and canreg data
    dt_both <- canreg_merge_CI5_registry(dt_report,dt_CI5_data, registry_region = ls_args$sr, registry_label = ls_args$header )
    
    #create bar chart graphique 
    setkeyv(dt_both, c("CSU_RANK", "SEX","asr"))
    dt_both[country_label!=ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
    dt_both[country_label==ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
    
    CI5_registries <- sort(as.character(unique(dt_both$country_label)))
    CI5_registries <- CI5_registries[CI5_registries != ls_args$header]
    CI5_registries <- gsub("\\*","",CI5_registries)
    
    doc <- body_add_par(doc,
                        paste0("Figure ",list_number$fig," shows a comparison of the age-standardised incidence rates in ",
                               ls_args$header, " with those observed in ", 
                               CI5_registries[1], ", ", CI5_registries[2],
                               " and ", CI5_registries[3], " (CI5 X, 2013)." ))
    
    
    doc <- body_add_par(doc, "\r\n")
    
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = TRUE,list_graph = TRUE,
                  FUN=canreg_bar_CI5_compare,
                  dt=dt_both,xtitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=",")))
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
    temp <- data.frame(1,2)

    for (i in 1:5) {
      
      ft <- flextable(temp)
      ft <- width(ft, width = 3.2)
      ft <- compose(ft,
                    i = 1, j = 1,
                    value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, sprintf("%03d",(2*i)-1), ".png"), width=3.2,height=3.2*dims[1]/dims[2])))
      
      ft <- compose(ft,
                    i = 1, j = 2,
                    value = as_paragraph(as_image(src = paste0(tempdir(), "\\temp_graph", list_number$fig, sprintf("%03d",(2*i)), ".png"), width=3.2,height=3.2*dims[1]/dims[2])))
      
      ft <- delete_part(ft, part = "header")
      ft <- border(ft,  border=fp_border(width=0))
      
      doc <- body_add_flextable(doc,ft, align = "center")
    }
    
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Comparison with other registries"))
    list_number$fig=list_number$fig+1
    
    doc <- body_add_par(doc, "\r\n")
    
  }
  

  ## Basis of diagnosis
  doc <- body_add_break(doc)
  doc <- body_add_par(doc, "Basis of Diagnosis (DCO / Clinical / MV) by site", style = "heading 2")
  incProgress(1/10, detail = "Basis of Diagnosis (DCO / Clinical / MV) by site")
  
  
  dt_basis <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX", "BASIS"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
  )
  
  dt_basis[BASIS > 0 & BASIS < 5, BASIS:=1]
  dt_basis[BASIS >= 5, BASIS:=2]
  
  dt_report <- canreg_ageSpecific_rate_data(dt_basis, keep_basis = TRUE)
  dt_report <- canreg_basis_table(dt_report)
  
  
  
  ft <- flextable(dt_report)
  ft <- set_header_labels(ft, CSU_label = "", CSU_ICD = "", total_cases= "",total_pc_test = "", 
                          BASIS_pc.0= "% DCO", BASIS_pc.1= "% Clinical", BASIS_pc.2= "% M.V") 
  ft <- add_header(ft, CSU_label = "Cancer site", CSU_ICD = "ICD-10", total_cases= "No. Cases",total_pc_test = "% total", 
                   BASIS_pc.0= "Basis of diagnosis", BASIS_pc.1= "Basis of diagnosis", BASIS_pc.2= "Basis of diagnosis",
                   top = TRUE ) 
  
  
  ft <- display( ft, col_key = "total_cases", pattern = "{{cases}}", 
                 formatters = list(cases ~ sprintf("%.00f", total_cases))) 
  
  ft <- width(ft, j = 1, width = 1.7)
  ft <- width(ft, j = 2, width = 1.2)
  
  ft<- merge_h(ft,i=1, part="header")
  ft <- border(ft ,j=1:4,border=fp_border(width = 0), part="header")
  ft <- border(ft ,i=1,j=1:4,border.top=fp_border(width = 1), part="header")
  ft <- border(ft ,border=fp_border(width = 0), part="body")
  ft <- border(ft, i=c(1,nrow(dt_report)),border.top=fp_border(width = 1), part="body")
  ft <- border(ft, i=nrow(dt_report),border.bottom=fp_border(width = 1), part="body")
  ft <- border(ft, j=c(1,3,5,6,7),border.left=fp_border(width = 1), part="all")
  ft <- border(ft, j=7,border.right=fp_border(width = 1), part="all")
  ft <- align(ft, align="center", part="header")
  ft <- height(ft, height = 0.1, part="header")
  ft <- bg(ft, i = seq(1,nrow(dt_report),2), bg="#deebf7", part = "body")
  ft <- bg(ft, i = nrow(dt_report), bg="#c6dbef", part = "body")
  ft <- bg(ft, i = 1, bg="#c6dbef", part = "header")
  ft <- bg(ft, i = 2, bg="#c6dbef", part = "header")
  
  doc <- body_add_par(doc, "\r\n")
  
  doc <- body_add_par(doc,
                      paste0("Table ",list_number$tbl," shows the percentage of cases at the major sites that were registered on the basis of information from a death certificate only (DCO) and with morphological verification (MV) - that is, based on cytology or histology (of the primary tumor, or a metastasis)."))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_par(doc,paste0("Table ",list_number$tbl,"."), style="centered")
  
  
  
  dt_appendix <- canreg_report_template_extract(report_path, script.basename, appendix  =TRUE)
  list_number$fig <- 1
  list_number$tbl <- 1
  
  if (!is.null(dt_appendix)) {
  	incProgress(1/10, detail = "add Appendix")
    list_number <- canreg_report_chapter_txt(dt_appendix, doc, report_path,dt_base,pop_file =ls_args$pop,list_number, appendix=TRUE)
  }
  
  print(log_file)
  print(doc, log_file)
  
  

}
