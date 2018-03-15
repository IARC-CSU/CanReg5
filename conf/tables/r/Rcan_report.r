  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  
  ## Load Rcan function
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  
  ## to get canreg argument list
  Args <- commandArgs(TRUE)
  ls_args <- canreg_args(Args)
  
  
tryCatch({
  
  #load dependency packages
  canreg_load_packages(c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png"), Rcan_source=script.basename)
  
  #merge incidence and population
  dt_all <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  time_limit <- 9
  graph_width <- 6
  dim_width = 2.7
  
  year_info <- canreg_get_years(dt_all)
  
  ## get age group label
  canreg_age_group <- canreg_get_agegroup_label(dt_all, ls_args$agegroup)
  
  ## get age group for cumulative risk 
  temp <- min(as.numeric(substr(ls_args$agegroup,regexpr("-", ls_args$agegroup)[1]+1,nchar(ls_args$agegroup))),14)
  canreg_age_group_cr <- canreg_get_agegroup_label(dt_all, paste0("0-",temp))
  
  
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
  
  
  list_number <- list(fig=1, tbl=1)
  
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  doc <- read_docx(paste(sep="/", script.basename,"slide_template", "template.docx"))
  
  doc <- body_add_par( doc, value = ls_args$header, style="TitleDoc")
  #oups need solution to add real title
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_toc(doc)
  
  
  
  
  dt_chapter <- canreg_report_template_extract(report_path, script.basename)
  
  if (year_info$span <= time_limit) {
    dt_chapter <- dt_chapter[title != "Estimated annual percentage change"]
  }
  
  list_number <- canreg_report_chapter_txt(dt_chapter, doc, report_path,dt_all,list_number)
  
  
  doc <- body_add_break(doc)
  doc <- body_add_par(doc, "Results", style =  paste("heading",1))
  
  
  
  # bar chart age
  dt_report <- dt_all
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
  
  
  #add warning if  many missing cases
  # sex_missing <- canreg_desc_missing_sex(ls_args$inc)
  # if (sex_missing$percent_missing > 0) {
  #   
  #   warning_note <- Footnote()
  #   warning <- paste0(sex_missing$nb_missing ," cases over ", sex_missing$nb_total," (",
  #                     sex_missing$percent_missing,"%) missed sex information. Sex have been attributed randomly based on known distribution")
  #   warning_note <- addParagraph(warning_note,warning)
  #   pot_intro <- pot(paste0(text_year,total_cases," cases"),footnote =warning_note)+
  #     pot(paste0(" of cancers were registered: ",total_male," among men and ",total_female," among women."))
  #   
  # } else {
  #   pot_intro <- pot(paste0(text_year,total_cases," cases of cancers were registered: ",
  #                           total_male," among men and ",total_female," among women."))
  # }
  
  # doc <- addParagraph(doc,pot_intro)
  doc <- body_add_par(doc,paste0(text_year,total_cases," cases of cancers were registered: ",
                                 total_male," among men and ",total_female," among women."))
  
  doc <- body_add_par(doc, "Number of cases in period, by age group & sex", style = "heading 2")
  
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph_a",list_number$fig ),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_cases_age_bar,
                df_data =dt_report,
                canreg_header = "", skin=FALSE)
  
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,"a. Bar chart, distribution of cases by age group and sex"))
  
  dt_report <- dt_all
  dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
  dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
  dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
  dt_report <- canreg_age_cases_data(dt_all, skin=TRUE)
  
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph_b", list_number$fig),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_age_cases_pie_multi_plot,
                dt=dt_report,
                canreg_header = "All cancers but C44")
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc,paste0("Fig ",list_number$fig,"b. Pie chart, distribution of cases by age group and sex"))
  list_number$fig <- list_number$fig +1
  
  doc <- body_add_par(doc, "Number of cases by year", style = "heading 2")
  
  dt_report <- dt_all
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
  
  dt_report <- dt_all
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
                ytitle=paste0("Number of cases, ", canreg_age_group$label))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_a", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,"a. Top 10 cancers, both sexes (Number of cases)"))
  
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph_b", list_number$fig),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_bar_top,
                df_data=dt_report,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
                canreg_header = "",
                ytitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png")), "dim" )
  doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph_b", list_number$fig, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2],style="centered" )
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,"b. Top 10 cancers, both sexes (Age-standardized rate per 100,000)"))
  list_number$fig=list_number$fig+1
  
  doc <- body_add_par(doc, "\r\n")
  
  
  
  dt_report <- dt_all
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = FALSE)
  
  
  ##calcul of ASR
  dt_asr <- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                                first_age = canreg_age_group$first_age+1,
                                last_age= canreg_age_group$last_age+1,
                                pop_base_count = "REFERENCE_COUNT",
                                age_label_list = "AGE_GROUP_LABEL")
  
  ##calcul of cumulative risk
  dt_cum_risk <- csu_cum_risk_core(df_data = dt_report,var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                   group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                                   last_age= canreg_age_group_cr$last_age+1,
                                   age_label_list = "AGE_GROUP_LABEL")
  
  dt_bar <- dt_asr
  var_top <- "CASES"
  digit <- 0
  xtitle <- paste0("Number of cases, ", canreg_age_group$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- display(ft,
                i = NULL, col_key = "X1", pattern= "{{dummy1}}",
                formatters = list(dummy1 ~ as_image(X1,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- display(ft,
                i = NULL, col_key = "X2", pattern= "{{dummy2}}",
                formatters = list(dummy2 ~ as_image(X2,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Top 10 cancers, number of cases"))
  list_number$fig = list_number$fig+1
  
  
  var_top <- "asr"
  digit <- 1
  xtitle<-paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- display(ft,
                i = NULL, col_key = "X1", pattern= "{{dummy1}}",
                formatters = list(dummy1 ~ as_image(X1,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- display(ft,
                i = NULL, col_key = "X2", pattern= "{{dummy2}}",
                formatters = list(dummy2 ~ as_image(X2,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Top 10 cancers, ASR"))
  list_number$fig <- list_number$fig+1
  
  var_top <- "cum_risk"
  digit <- 2
  dt_bar <- dt_cum_risk
  xtitle<-paste0("Cumulative incidence risk (percent), ", canreg_age_group_cr$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
  
  temp <- data.frame(1,2)
  ft <- flextable(temp)
  ft <- width(ft, width = dim_width)
  ft <- display(ft,
                i = NULL, col_key = "X1", pattern= "{{dummy1}}",
                formatters = list(dummy1 ~ as_image(X1,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- display(ft,
                i = NULL, col_key = "X2", pattern= "{{dummy2}}",
                formatters = list(dummy2 ~ as_image(X2,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Top 10 cancers, cumulative risk, 0-74 years"))
  list_number$fig <- list_number$fig+1
  
  doc <- body_add_break(doc)
  
  doc <- body_add_par(doc, "Age-specific incidence rates (most common sites) by sex", style = "heading 2")
  
  
  dt_report <- dt_all
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
  ft <- display(ft,
                i = NULL, col_key = "X1", pattern= "{{dummy1}}",
                formatters = list(dummy1 ~ as_image(X1,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- display(ft,
                i = NULL, col_key = "X2", pattern= "{{dummy2}}",
                formatters = list(dummy2 ~ as_image(X2,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
  
  ft <- delete_part(ft, part = "header")
  ft <- border(ft,  border=fp_border(width=0))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Age-specific incidence rates"))
  list_number$fig <- list_number$fig+1
  
  
  if (year_info$span >  time_limit) {
    
    doc <- body_add_break(doc)
    doc <- body_add_par(doc, "Trend in ASR (most common sites) by sex", style = "heading 2")
    
    dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
    
    ##calcul of ASR
    dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                                    first_age =canreg_age_group$first_age+1,
                                    last_age= canreg_age_group$last_age+1,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
    
    
    #produce graph
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = FALSE,list_graph = TRUE,
                  FUN=canreg_asr_trend_top,
                  dt=dt_report,number = 5,
                  canreg_header = "",
                  ytitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
    
    temp <- data.frame(1,2)
    ft <- flextable(temp)
    ft <- width(ft, width = dim_width)
    ft <- display(ft,
                  i = NULL, col_key = "X1", pattern= "{{dummy1}}",
                  formatters = list(dummy1 ~ as_image(X1,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
    
    ft <- display(ft,
                  i = NULL, col_key = "X2", pattern= "{{dummy2}}",
                  formatters = list(dummy2 ~ as_image(X2,src = paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"), width=dim_width,height=dim_width*dims[1]/dims[2])))
    
    ft <- delete_part(ft, part = "header")
    ft <- border(ft,  border=fp_border(width=0))
    
    doc <- body_add_par(doc, "\r\n")
    doc <- body_add_flextable(doc,ft, align = "center")
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Trend in Age-standardized (W) incidence rate"))
    list_number$fig <- list_number$fig+1
    
  }
  
  if (year_info$span >  time_limit) {
    
    doc <- body_add_break(doc)
    doc <- body_add_par(doc, "Estimated annual percentage change", style = "heading 2")
    
    dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
    
    
    ##calcul of ASR
    dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX", "YEAR"), missing_age = canreg_missing_age(dt_all),
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
                  canreg_header = "Estimated Average Percentage Change",
                  ytitle=paste0("Estimated average percentage change (%), ", canreg_age_group$label))
    
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
    doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png"),width=graph_width*0.9,height=graph_width*0.9*dims[1]/dims[2],style="centered" )
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Estimated annual percentage change, male"))
    list_number$fig=list_number$fig+1 
    doc <- body_add_img(doc, paste0(tempdir(), "\\temp_graph", list_number$fig, "002.png"),width=graph_width*0.9,height=graph_width*0.9*dims[1]/dims[2],style="centered" )
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Estimated annual percentage change, female"))
    list_number$fig=list_number$fig+1 
    doc <- body_add_par(doc, "\r\n")
    
  }
  
  
  
  ## comparaison with CI5 registries.
  
  region_admit <- c("EastMed", "Americas", "West Pacific", "Europe", "SEAsia", "Africa")
  
  if (ls_args$sr %in% region_admit) {
    
    doc <- body_add_break(doc)
    doc <- body_add_par(doc, "Comparison of summary rates with other registries (in same region)", style = "heading 2")
    
    doc <- body_add_par(doc, "\r\n")
    
    
    dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
    
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
                        paste0("Figure ",list_number$fig," shows a comparaison of the age standardised incidence rates in ",
                               ls_args$header, " with those observed in ", 
                               CI5_registries[1], ", ", CI5_registries[2],
                               " and ", CI5_registries[3], " (CI5 X, 2013)." ))
    
    
    doc <- body_add_par(doc, "\r\n")
    
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", list_number$fig),landscape = TRUE,list_graph = TRUE,
                  FUN=canreg_bar_CI5_compare,
                  dt=dt_both,xtitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=",")))
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", list_number$fig, "001.png")), "dim" )
    
    for (i in 1:5) {
      
      temp <- data.frame(1,2)
      ft <- flextable(temp)
      ft <- width(ft, width = 3.2)
      ft <- display(ft,
                    i = NULL, col_key = "X1", pattern= "{{dummy1}}",
                    formatters = list(dummy1 ~ as_image(X1,src = paste0(tempdir(), "\\temp_graph", list_number$fig, sprintf("%03d",(2*i)-1), ".png"), width=3.2,height=3.2*dims[1]/dims[2])))
      
      ft <- display(ft,
                    i = NULL, col_key = "X2", pattern= "{{dummy2}}",
                    formatters = list(dummy2 ~ as_image(X2,src = paste0(tempdir(), "\\temp_graph", list_number$fig, sprintf("%03d",(2*i)), ".png"), width=3.2,height=3.2*dims[1]/dims[2])))
      
      ft <- delete_part(ft, part = "header")
      ft <- border(ft,  border=fp_border(width=0))
      
      doc <- body_add_flextable(doc,ft, align = "center")
    }
    
    doc <- body_add_par(doc, paste0("Fig ",list_number$fig,". Comparaison with other registries"))
    list_number$fig=list_number$fig+1
    
    doc <- body_add_par(doc, "\r\n")
    
  }
  
  
  
  
  
  ## Basis of diagnosis
  doc <- body_add_break(doc)
  doc <- body_add_par(doc, "Basis of Diagnosis (DCO / Clinical / MV) by site", style = "heading 2")
  
  
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
                      paste0("Table ",list_number$tbl," shows the percentage of cases at the major sites that were registered on the basis of information from a death certificate only (DCO) and with morphological verification (MV) - that is, based on cytology or histology (of the primary tumour, or a metastasis)."))
  
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_flextable(doc,ft, align = "center")
  doc <- body_add_par(doc, "\r\n")
  doc <- body_add_par(doc,paste0("Table ",list_number$tbl,"."), style="centered")
  
  
  
  dt_appendix <- canreg_report_template_extract(report_path, script.basename, appendix  =TRUE)
  list_number$fig <- 1
  list_number$tbl <- 1
  
  if (!is.null(dt_appendix)) {
    list_number <- canreg_report_chapter_txt(dt_appendix, doc, report_path,dt_all,list_number, appendix=TRUE)
  }
  
  
  print(doc, ls_args$filename)
  
  
  reporteRs_OO_patched(docx=ls_args$filename)
  

#talk to canreg
  cat(paste("-outFile",ls_args$filename,sep=":"))
	
	
  },
  
  error = function(e){
    if (exists("doc")) {
     writeDoc(doc, file = ls_args$filename)
     if (file.exists(ls_args$filename)) file.remove(ls_args$filename)
    }
    
    canreg_error_log(e,ls_args$filename,ls_args$out,Args,ls_args$inc,ls_args$pop)
  }
)
	
	
	
	
