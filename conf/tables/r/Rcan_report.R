## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
  Args <- commandArgs(TRUE)
  
  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  source(paste(sep="/", script.basename, "Rcan_source.r"))
  ################

  
	
  graph_width <- 5


  
  ext <- substring(filename,regexpr("\\.[^\\.]*$", filename)+1)
  if (ext != "docx") {
    filename <- paste0(substr(filename, 1,nchar(filename)-nchar(ext)), "docx")

  }
  

doc <- docx()

doc <- addParagraph( doc, value = header, stylename =  "TitleDoc")
doc <- addParagraph(doc, "\r\n")
doc <- addParagraph(doc, "\r\n")
doc <- addTOC(doc)

doc <- addPageBreak(doc) # go to the next page

doc <- addTitle(doc, "Registry background and population", level=1)
doc <- addTitle(doc, "Background", level=2)

text <- canreg_import_txt("Background.txt", folder = paste(sep="/", script.basename, "report_text/"))
if (!grepl("NA\n", text)) {
  doc <- addParagraph(doc,  text) 
} 

doc <- addTitle(doc, "Population", level=2)

dt_report <- dt_all
dt_report <- canreg_pop_data(dt_report)

text <- canreg_import_txt("Population.txt", folder = paste(sep="/", script.basename, "report_text/"))
if (!grepl("NA\n", text)) {
  doc <- addParagraph(doc,  text) 
} 
doc <- addParagraph(doc, "\r\n")

dims <- attr( png::readPNG(paste0(paste(sep="/", script.basename, "report_text/"), "map_general.png")), "dim" )
doc <- addImage(doc, paste0(paste(sep="/", script.basename, "report_text/"), "map_general.png"),width=3,height=3*dims[1]/dims[2],par.properties = parProperties(text.align = "left"))
doc <- addParagraph(doc, "Fig 1. Region map")
doc <- addPageBreak(doc) # go to the next page

total_pop <- formatC(round(unique(dt_report$Total)), format="d", big.mark=",") 
total_male <- formatC(round(dt_report[SEX==levels(SEX)[1], sum(COUNT)]), format="d", big.mark=",")
total_female <- formatC(round(dt_report[SEX==levels(SEX)[2], sum(COUNT)]), format="d", big.mark=",")

doc <- addParagraph(doc,
                    paste0("The average annual population was ", total_pop,
                           " (",total_male," males and ",total_female, " females).\n"))

canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = FALSE,
              FUN=canreg_population_pyramid,
              df_data =dt_report,
              canreg_header = "")

dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph.png")), "dim" )
doc <- addImage(doc, paste0(tempdir(), "\\temp_graph.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc, "Fig 2. Estimated average annual population")

doc <- addPageBreak(doc)

doc <- addTitle(doc, "Methods", level=1)   


text <- canreg_import_txt("Methods.txt", folder = paste(sep="/", script.basename, "report_text/"))
if (!grepl("NA\n", text)) {
  doc <- addParagraph(doc,  text) 
} 

doc <- addTitle(doc, "Source of data", level=2)   
doc <- addTitle(doc, "ect..", level=2)   
doc <- addTitle(doc, "Variable", level=2)   
doc <- addParagraph(doc, "Can generate some table from canreg?")
doc <- addTitle(doc, "Classification and coding", level=2)   
doc <- addParagraph(doc, "Can generate table from canreg ICD03 classification and label")                 
doc <- addTitle(doc, "The database", level=2)  
doc <- addParagraph(doc, "The registry uses CANREG (version 6, vintage edition) for data entry, management and analysis")                 
doc <- addTitle(doc, "etc..", level=2)  

doc <- addPageBreak(doc)

doc <- addTitle(doc, "Results", level=1)      

# bar chart age
dt_report <- dt_all
dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U" 
dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified" 
dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
dt_report <- canreg_age_cases_data(dt_report,age_group = c(5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80), skin=TRUE)

total_cases <- formatC(dt_report[,sum(CASES)], format="d", big.mark=",")
total_male <- formatC(dt_report[SEX == "Male",sum(CASES)], format="d", big.mark=",")
total_female <-formatC(dt_report[SEX == "Female",sum(CASES)], format="d", big.mark=",")


doc <- addParagraph(doc,
                    paste0(total_cases," cases of cancers were registered, ",
                           total_male," among men and ",total_female," among women."))

doc <- addTitle(doc, "Number of cases in period, by age group & sex", level = 2)


canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = FALSE,
              FUN=canreg_cases_age_bar,
              df_data =dt_report,
              canreg_header = "", skin=FALSE)


dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph.png")), "dim" )
doc <- addImage(doc, paste0(tempdir(), "\\temp_graph.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc, "Fig 3a. Bar chart, distribution of cases by age group and sex")

dt_report <- dt_all
dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U" 
dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified" 
dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
dt_report <- canreg_age_cases_data(dt_all, skin=TRUE)


canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = FALSE,
              FUN=canreg_age_cases_pie_multi_plot,
              dt=dt_report,
              canreg_header = "All cancers but C44")

dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph.png")), "dim" )
doc <- addImage(doc, paste0(tempdir(), "\\temp_graph.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc,"Fig 3b. Pie chart, distribution of cases by age group and sex")


doc <- addTitle(doc, "The most common cancers, by sex", level = 2)

dt_report <- dt_all
dt_report <- dt_report[ICD10GROUP != "C44",]
dt_report <- dt_report[ICD10GROUP != "O&U",]

dt_report <- canreg_ageSpecific_rate_data(dt_report, keep_ref = TRUE)

temp_max <- max(dt_report$AGE_GROUP)
temp_min <- min(dt_report$AGE_GROUP)
temp1 <- as.character(unique(dt_report[dt_report$AGE_GROUP == temp_min,]$AGE_GROUP_LABEL))
temp2 <-as.character(unique(dt_report[dt_report$AGE_GROUP == temp_max,]$AGE_GROUP_LABEL))
temp1 <- substr(temp1,1,regexpr("-", temp1)[1]-1)
temp2 <- substr(temp2,regexpr("-", temp2)[1]+1,nchar(temp2))
canreg_age_group <- paste0(temp1,"-",temp2, " years")

dt_report<- csu_asr_core(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                      var_by = c("cancer_label", "SEX"),
                      missing_age = canreg_missing_age(dt_report),
                      first_age = temp_min+1,
                      last_age= temp_max+1,
                      pop_base_count = "REFERENCE_COUNT",
                      age_label_list = "AGE_GROUP_LABEL")



text_male <- canreg_report_top_cancer_text(dt_report, 5, sex_select="Male")
text_female <- canreg_report_top_cancer_text(dt_report, 5, sex_select="Female")

doc <- addParagraph(doc, paste0("In men ",tolower(text_male)))
doc <- addParagraph(doc, paste0("In women ",tolower(text_female)))

canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = FALSE,
              FUN=canreg_bar_top,
              df_data=dt_report,
              var_top="CASES",
              nsmall=0,
              color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
              canreg_header = "",
              ytitle=paste0("Number of cases, ", canreg_age_group))

dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph.png")), "dim" )
doc <- addImage(doc, paste0(tempdir(), "\\temp_graph.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc, "Fig 4a. Top 10 cancers, both sexes (Number of cases)")


canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = FALSE,
              FUN=canreg_bar_top,
              df_data=dt_report,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
              canreg_header = "",
              ytitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group))

dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph.png")), "dim" )
doc <- addImage(doc, paste0(tempdir(), "\\temp_graph.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc, "Fig 4b. Top 10 cancers, both sexes (Age-standardized rate per 100,000)")

doc <- addTitle(doc, "Age specific incidence rates (most common sites) by sex", level = 2)

dt_report <- dt_all
dt_report <- dt_report[ICD10GROUP != "C44",]
dt_report <- dt_report[ICD10GROUP != "O&U",]
dt_report <- canreg_ageSpecific_rate_data(dt_report)

canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = TRUE,
              FUN=canreg_ageSpecific_rate_top,
              dt=dt_report,log_scale = TRUE,nb_top = 5,
              canreg_header = "")

dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph001.png")), "dim" )
doc <- addImage(doc, paste0(tempdir(), "\\temp_graph001.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc, "Fig 5a. Age specific incidence rates, men")

doc <- addImage(doc, paste0(tempdir(), "\\temp_graph002.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
doc <- addParagraph(doc, "Fig 5b. Age specific incidence rates, women")

writeDoc(doc, file = filename)

##talk to canreg
cat(paste("-outFile",filename,sep=":"))
	
	
