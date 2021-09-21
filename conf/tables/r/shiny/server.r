## version : 1.1


shinyServer(function(input, output, session) {

  #app close when the session is stopped  
  session$onSessionEnded(function() {
    cat("back to static life")
    stopApp()
    q("no")
  })
  



  file_data <- "data/"
  file_pptx <-paste(sep="/","slide_template", "canreg_template.pptx")
  file_utf8 <- ""
  
  #Change for shinyappio: 
  #file_utf8 <- "_UTF8"
  
  
  #Parametre and fixed variable
  
  #dataset

  
  
  graph_width <- 8
  graph_width_vertical <- 5
	graph_width_split <- 4
  
  
  #reactive values init
  values <- reactiveValues(doc = NULL, nb_slide = 0, text= "Slide included")
  bool_rv <- reactiveValues(trigger1=FALSE)
  progress_bar <- reactiveValues(object=NULL)
  table <- reactiveValues(label="")

  output$UI_regtitle <- renderText({ls_args$header})
  output$directorypath <- renderText({download_dir})



	output$UI_select_table <- renderUI({
	
		table_list <- c( 
			"Automatic report" = 0,
			"Population pyramid" = 1,
			"Barchart of cases by age group by sex" = 2, 
			"piechart of cases by age group by sex" = 3, 
			"Top cancer both sexes" = 4 ,
			"Top cancer by sexes" = 5 ,
			"Age-specific rates (Top Cancer Sites)" = 6,
			"Age-specific rate by cancer sites" = 7,
			"Barchart of cases by year" = 8,
			"CI5 XI comparaison" = 12,
			"Childhood cancers table" = 13)
			
			if (year_info$span >= 3) {
			
				table_list = c(table_list, "Time trends (Top cancer Sites)" = 9)
				table_list = c(table_list, "Estimated Average Percentage Change" = 10)
				table_list = c(table_list, "Time trends by cancer sites" = 11)
			
			} 
			

			
		selectInput("select_table", NULL,selected = 4, table_list)


	})

	output$UI_select_format <- renderUI({
	
		table_list <- c( "pdf","tiff","png", "svg", "ps","csv")
		if (!is.null(input$select_table))
		{
			if (input$select_table %in% c(13))
			{
				table_list <- c( "csv")
			}
		}
				
			
		selectInput("select_format", NULL,selected = 1, table_list)


	})		
  
  output$UI_nbSlide <- renderUI({
    
    
    if (values$nb_slide == 0) {

      values$text <- "Slide included:"
    } 
    else {
      
      values$text <- paste0(isolate(values$text), '<br>',isolate(table$label))
      
      if (isolate(input$select_table == 7)) {
        values$text <- paste(isolate(values$text), isolate(input$selectCancerSite))
      }
	  	else if (isolate(input$select_table == 11)) {
        values$text <- paste(isolate(values$text), isolate(input$selectCancerSite))
      }
			else if (isolate(input$select_table == 10)) {
			
				if (isolate(input$checkCI)) {
					values$text <- paste(isolate(values$text), "with CI")
				}
				
			}

    }
    
    tags$div(id="divSlidelist", class="mat_text", checked=NA,
             tags$p(HTML(isolate(values$text)))
    )
    
  })
  

	output$UI_control1 <- renderUI({
		
		if  (!is.null(input$select_table)) {

			if  (input$select_table %in% c(0)) {
				
			 checkboxInput("checkAnnexe", "Include annexe",FALSE)

			}  
			else if  (input$select_table %in% c(2,3,8)) {
				
			 radioButtons("radioSkin", "",
										 c("excluding C44 skin" = 1,
											 "including C44 skin" = 2)
				)
			
			}  
			
			else if  (input$select_table %in% c(4,5)) {
				
				radioButtons("radioValue", "Value:",
										 c("Age-standardized rate" = "asr",
											 "Number of cases" = "cases",
											 "Cumulative risk" = "cum")
				)
				
			}
			
			else if (input$select_table %in% c(6,7,9,11)) {
				
				radioButtons("radioLog", "y axes scale:",
										 c("Logarithmic" = "log",
											 "Linear" = "normal")
				)
				
			}
			else if (input$select_table %in% c(10)) {
				
				checkboxInput("checkCI", "Confidence interval",FALSE)

			}
			else if (input$select_table %in% c(12)) {
				
				
				cancer_list <-  unique(dt_base$cancer_label)
				cancer_list <- sort(as.character(cancer_list))

				
				selectInput("selectCancerSite", "Select cancer sites", cancer_list)
				
			
			}

		}
		
		
			
	})
	
	output$UI_control2 <- renderUI({
	
		if  (!is.null(input$select_table)) {
			if (input$select_table==2) {
				
					radioButtons("radioAgeGroup", "Age-group division:",
										 c("0-4,5-9,...,80-84,85+" = 1,
											 "0-14, 15-29,30-49,50-69,70+" = 2)
				)
				
			}
			else if (input$select_table %in% c(12)) {
				
				radioButtons("radioSex", "Sex:",
										 c("Males" = "Male",
											"Females" = "Female")
				)
				
			}  
		}
		
	})
	
	output$UI_control3 <- renderUI({
		
		if  (!is.null(input$select_table)) {
			if  (input$select_table %in% c(4,5,6,9,10)) {
			
				slide_min <- 3
				slide_max <- 20
				slide_def <- 10
				
				if (input$select_table %in%  c(6,9) ) {
				
					slide_min <- 1
					slide_max <- 10
					slide_def <- 5
				
				}
				else if (input$select_table %in%  c(10) ) {
					
					slide_min <- 1
					slide_max <- 25
					slide_def <- 20
				
				}
				
				sliderInput("slideNbTopBar", "Number of cancer sites:", slide_min, slide_max, slide_def)
				
				
			} 
			else if (input$select_table %in% c(7,11)) {
				
				
				cancer_list <-  unique(dt_base$cancer_label)
				n <- length(cancer_list)
				cancer_list <- sort(as.character(cancer_list))
				cancer_list <- cancer_list[1:(n-1)]
				
				selectInput("selectCancerSite", "Select cancer sites", cancer_list)
				
			
			}
		}
			
		
	})
	
	output$UI_control4 <- renderUI({
		
		if  (!is.null(input$select_table)) {
			if (input$select_table %in% c(4,5,9,10,11)) {
				sliderInput("slideAgeRange", "Age group:", 0, 90, c(0,90), step=5)
			}
		}

			
	})

	output$UI_control5 <- renderUI({
		
		if  (!is.null(input$select_table)) {
			if (input$select_table %in% c(12)) {
				
				
				dt_CI5_list <- readRDS(paste0(script.basename,"/r-sources", "/CI5_alldata.rds"))
				registry_list <-  unique(dt_CI5_list$country_label)
				registry_list <- as.character(registry_list)
				selectInput("selectRegistry1",NULL, registry_list, selected = dt_CI5_label[1])
				
			}
		}

			
	})

	output$UI_control6 <- renderUI({
		
		if  (!is.null(input$select_table)) {
			if (input$select_table %in% c(12)) {
				
				
				dt_CI5_list <- readRDS(paste0(script.basename,"/r-sources", "/CI5_alldata.rds"))
				registry_list <-  unique(dt_CI5_list$country_label)
				registry_list <- as.character(registry_list)
				selectInput("selectRegistry2",NULL , registry_list, selected = dt_CI5_label[2])
				
			}
		}

			
	})

	output$UI_control7 <- renderUI({
		
		if  (!is.null(input$select_table)) {
			if (input$select_table %in% c(12)) {
				
				
				dt_CI5_list <- readRDS(paste0(script.basename,"/r-sources", "/CI5_alldata.rds"))
				registry_list <-  unique(dt_CI5_list$country_label)
				registry_list <- as.character(registry_list)
				selectInput("selectRegistry3", NULL, registry_list, selected = dt_CI5_label[3])
				
			}
		}

			
	})
	

  observeEvent(input$select_table,{

  	hide(id="report_option", anim=TRUE)
  	hide(id="fluid_test", anim=TRUE)
  	hide(id="table", anim=TRUE)
  	show(id="plot", anim=TRUE)
  	show(id="export_menu", anim=TRUE)

  	if (input$select_table==0) {
      table$label <- "Automatic Report"
      show(id="report_option", anim=TRUE)
      hide(id="plot", anim=TRUE)
      hide(id="export_menu", anim=TRUE)
      show(id="controls_COL1", anim=TRUE)
      hide(id="controls_COL2", anim=TRUE)
      canreg_shiny_list_folder_content(output)
      
    }
    else if (input$select_table==1) {
      table$label <- "Population pyramid"
      hide(id="controls_COL1", anim=TRUE)
      hide(id="controls_COL2", anim=TRUE)
      
    }
	else if (input$select_table== 2) {
			table$label <- "Barchart by age and sex"
			show(id="controls_COL1", anim=TRUE)
			hide(id="controls_COL2", anim=TRUE)

			
	}
	else if (input$select_table== 3) {
			table$label <- "Piechart by age and sex"
			show(id="controls_COL1", anim=TRUE)
			hide(id="controls_COL2", anim=TRUE)
			
	}
	else if (input$select_table== 4) {
		table$label <- "Barchart Top cancer both sexes"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 5) {
		table$label <- "Barchart Top cancer by sexes"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 6) {
		table$label <- "Age-specific trend top cancer"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 7) {
		table$label <- "Age-specific trend"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 8) {
		table$label <- "Barchart by year"
		show(id="controls_COL1", anim=TRUE)
		hide(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 9) {
		table$label <- "Time trend top cancer"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 10) {
		table$label <- "Estimated Annual Percentage Change"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
		
	}
	else if (input$select_table== 11) {
		table$label <- "Time trends"
		show(id="controls_COL1", anim=TRUE)
		show(id="controls_COL2", anim=TRUE)
	}
	else if (input$select_table== 12) {
		table$label <- "CI5 XI comparison"
		show(id="controls_COL1", anim=TRUE)
		hide(id="controls_COL2", anim=TRUE)
		show(id="fluid_test", anim=TRUE)
	}
	else if (input$select_table== 13) {
		table$label <- "Childhood cancers table"
		hide(id="controls_COL1", anim=TRUE)
		hide(id="controls_COL2", anim=TRUE)
		hide(id="plot", anim=TRUE)
		show(id="table", anim=TRUE)
	}
  })
  
  observeEvent(values$nb_slide,{
    
    
    
    if (values$nb_slide==0) {
      hide(id="downloadPres", anim=TRUE)
      hide(id="pptx_filename", anim=TRUE)
      hide(id="UI_nbSlide", anim=TRUE)
    } else {
      if (!is.na(download_dir)) {
      	show(id="downloadPres", anim=TRUE)
      }
      show(id="pptx_filename", anim=TRUE)
      show(id="UI_nbSlide", anim=TRUE)
    } 
    
  })

  observeEvent(input$radioValue , {
    
    
    if (input$radioValue == "cum") {
      vals <- 75
    } else {
      vals <- 90
    }
    
    # If the slide range value are not update, the trigger1 is turn on, so the graph will be update
    bool_rv$trigger1 = input$slideAgeRange[2] == vals
    updateSliderInput(session, "slideAgeRange", "Age group:", value=c(0,90), min=0, max=vals,step=5)
    
  })
  

  
  #Calcul statistics
  dt_all <-  reactive({ 

			if (!is.null(dt_base) & !is.null(input$select_table)) {
				
				isolate(progress_bar$object)$set(value = 0, message = 'Please wait:', detail = 'Calculate statistics')
				

	
				dt_temp <- canreg_shiny_data(input, session)
			
				if (input$select_table %in% c(4,5,9,10)) {
					if (length(bool_rv$trigger1) != 0) {
						if (bool_rv$trigger1) {
							bool_rv$trigger1 <- FALSE
							}
						}
					}

				return(dt_temp)

		}
			else {
				return(NULL)
			}
		
  })


  

  #Render plot
  output$plot <- renderPlot({ 
    

    progress_bar$object <- Progress$new(session, min=0, max=100)
    on.exit(progress_bar$object$close())

    if (!is.null(dt_all()))  {
      
			
      isolate(progress_bar$object)$set(value = 50,  message = 'Please wait:', detail = 'Render graph')
      
			canreg_shiny_plot(dt_all(), input,session,  FALSE)
			
			isolate(progress_bar$object)$set(value = 100,  message = 'Please wait:', detail = 'Done')
	  
    }
    
  })

   #Render table
  output$table <-  renderTable(
  	{ 

	  	# dt_all_table()
	    if (!is.null(dt_all()))  {
	      
	      canreg_shiny_table(dt_all(), input,session,  FALSE)
		  	
	    }
    },
  	striped=TRUE,
    hover = TRUE,
    align = "ccrrrrrrrrrrr"
  )
  
  #Download file
  output$downloadFile <- 

	  downloadHandler(
	    
	    filename = function() {
	    
				bool_CI <- FALSE
				if (!is.null(input$checkCI)) {
					bool_CI <- input$checkCI
				}
				
				#multiple file
				bool_multiple <- canreg_shiny_multiple_output(input$select_table, bool_CI, input$select_format)
					
				if (bool_multiple) {
					temp <- paste0(input$text_filename, ".", "zip")
				}
				else {
					temp <- paste0(input$text_filename, ".", input$select_format)
				}
				return(temp)
	 
	    },
	    
	    content = function(file) {
			
			withProgress(message = 'Download output', value = 0, {
	      
				file_temp <- substr(file,1, nchar(file)-nchar(input$select_format)-1)

				if (input$select_table == 13)
				{
					canreg_shiny_table(dt_all(),  input,session, TRUE,FALSE,file_temp)
				}
				else {
					canreg_shiny_plot(dt_all(),  input,session, TRUE,FALSE,file_temp)
				}
				
				incProgress(1, detail = "")
				
				bool_CI <- FALSE
				if (!is.null(input$checkCI)) {
					bool_CI <- input$checkCI
				}
				bool_multiple <- canreg_shiny_multiple_output(input$select_table, bool_CI, input$select_format)
				
				if (bool_multiple) {
						
						file_male <- paste0(file_temp, "001", ".", input$select_format)
						file_female <- paste0(file_temp, "002", ".", input$select_format)
						
						tempfile1 <- paste0(input$text_filename, "-male.", input$select_format)
						tempfile2 <- paste0(input$text_filename, "-female.", input$select_format)
						
						file.copy(file_male, tempfile1, overwrite = TRUE)
						file.copy(file_female,tempfile2, overwrite = TRUE)
						
						zip(file, c(tempfile1, tempfile2))
						
						file.remove(c(tempfile1,tempfile2))

				 }


			})
	      
		})

  #Action button: Add slide
  observeEvent(input$actionSlide,{ 

		if (values$nb_slide == 0) {
        
			withProgress(message = 'create powerpoint', value = 0, {
					values$doc <- read_pptx(file_pptx)
					incProgress(1, detail = "")
					})
     }
      
	  withProgress(message = 'add powerpoint slide', value = 0, {
      filename <- paste0(tempdir(), "\\temp_graph",values$nb_slide+1)  
      
			canreg_shiny_plot(dt_all(),  input,session, TRUE,TRUE,filename)
			incProgress(1, detail = "")
			
			if (input$select_table == 1) {
							

				values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Population pyramid", location = ph_location_type(type =  "title"))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  			 values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
			} 
			else if (input$select_table==2) {
				
				values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Number of cases by age group & sex", location = ph_location_type(type =  "title"))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
				
			}
			else if (input$select_table==3) {
				

				values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Proportion of cases by age group & sex", location = ph_location_type(type =  "title"))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
				
			}
			else if (input$select_table==4) {
				

				str_temp <- paste0("top ", isolate(input$slideNbTopBar), " cancers, both sexes")
				
				values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  str_temp, location = ph_location_type(type =  "title"))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
				
			}
			 else if (input$select_table==5) {
				

				str_temp <- paste0("top ", isolate(input$slideNbTopBar), " cancers")
				
				values$doc <-  add_slide(values$doc, layout="Canreg_split", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  str_temp, location = ph_location_type(type =  "title"))

				dims <- attr( png::readPNG (paste0(filename, "001.png")), "dim" )
				temp_img1 <- external_img(src = paste0(filename, "001.png"), height =graph_width_split*dims[1]/dims[2],width = graph_width_split)
				temp_img2 <- external_img(src = paste0(filename, "002.png"), height =graph_width_split*dims[1]/dims[2],width = graph_width_split)
  			values$doc <- ph_with(values$doc,value = temp_img1,location = ph_location_type(type = "body", id=1), use_loc_size =FALSE)
  			values$doc <- ph_with(values$doc,value = temp_img2,location = ph_location_type(type = "body", id=2), use_loc_size =FALSE)

				
			}

			else if (input$select_table==6) {

				
				dims <- attr( png::readPNG (paste0(filename, "001.png")), "dim" )

				values$doc <-  add_slide(values$doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Age-specific rates:\r\nMales", location = ph_location_type(type =  "title"))			
				temp_img <- external_img(src = paste0(filename, "001.png"), height =graph_width_vertical*dims[1]/dims[2],width = graph_width_vertical)
				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)

				
				values$doc <-  add_slide(values$doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Age-specific rates:\r\nFemales", location = ph_location_type(type =  "title"))
				temp_img <- external_img(src = paste0(filename, "002.png"), height =graph_width_vertical*dims[1]/dims[2],width = graph_width_vertical)
				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
					
			}
			
			else if (input$select_table==7) {
				
				str_temp <- paste0("Age-specific rates:\r\n", isolate(input$selectCancerSite))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				values$doc <-  add_slide(values$doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  str_temp, location = ph_location_type(type =  "title"))
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width_vertical*dims[1]/dims[2],width = graph_width_vertical)
				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
			}
			else if (input$select_table==8) {
				
				values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Number of cases by year", location = ph_location_type(type =  "title"))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  			values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
				
			}
			else if (input$select_table==9) {

			
				dims <- attr( png::readPNG (paste0(filename, "001.png")), "dim" )

				values$doc <-  add_slide(values$doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "Trend in ASR:\r\nMales", location = ph_location_type(type =  "title"))
				temp_img <- external_img(src = paste0(filename, "001.png"), height =graph_width_vertical*dims[1]/dims[2],width = graph_width_vertical)
				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)


				values$doc <-  add_slide(values$doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with_text(values$doc, type = "title",  str = "Trend in ASR:\r\nFemales")
				temp_img <- external_img(src = paste0(filename, "002.png"), height =graph_width_vertical*dims[1]/dims[2],width = graph_width_vertical)
				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)


			}
			else if (input$select_table==10) {

				if (input$checkCI) {
					
					dims <- attr( png::readPNG (paste0(filename, "001.png")), "dim" )

					values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
					values$doc <- ph_with(values$doc, value =  "Estimated annual percentage change:\r\nMales", location = ph_location_type(type =  "title"))
					temp_img <- external_img(src = paste0(filename, "001.png"), height =graph_width*dims[1]/dims[2],width = graph_width)
					values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)

					values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
					values$doc <- ph_with(values$doc, value =  "Estimated annual percentage change:\r\nFemales", location = ph_location_type(type =  "title"))
					temp_img <- external_img(src = paste0(filename, "002.png"), height =graph_width*dims[1]/dims[2],width = graph_width)
					values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
				}
				else {
					values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
					values$doc <- ph_with(values$doc, value =  "Estimated annual percentage change", location = ph_location_type(type =  "title"))
					dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
					temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  					values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)

				}
				
			}
			else if (input$select_table==11) {
				
				str_temp <- paste0("Time trends:\r\n", isolate(input$selectCancerSite))
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				values$doc <-  add_slide(values$doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  str_temp, location = ph_location_type(type =  "title"))
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width_vertical*dims[1]/dims[2],width = graph_width_vertical)
  				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
			}
			else if (input$select_table==12) {
				
				dims <- attr( png::readPNG (paste0(filename, ".png")), "dim" )
				values$doc <-  add_slide(values$doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
				values$doc <- ph_with(values$doc, value =  "CI5 XI comparison", location = ph_location_type(type =  "title"))
				temp_img <- external_img(src = paste0(filename, ".png"), height =graph_width*dims[1]/dims[2],width = graph_width)
  				values$doc <- ph_with(values$doc,value = temp_img,location = ph_location_type(type = "body"), use_loc_size =FALSE)
				
			}
			else if (input$select_table==13) {
				
				values$doc <-  add_slide(values$doc, layout="Canreg_basic_wide", master="Office Theme") ## add PPTX slide (Title + content)
  			values$doc <- ph_with(values$doc, value =  "Childhood cancers (0 to 14 years)", location = ph_location_type(type =  "title"))
  			dt_temp <- copy(dt_iccc)
  			table_iccc <- canreg_iccc_table(dt_temp)
  			dt_report <- table_iccc$dt
  			age_label <- table_iccc$age_label_order

  			ft <- flextable(dt_report)
			  ft <- set_header_labels(ft, ICCC_code = "", ICCC_label = "", 
			    CSU_C.0= age_label[1],
			    CSU_C.1= age_label[2],
			    CSU_C.2= age_label[3],
			    total_cases = "All", 
			    ratio = "M/F",
			    frequence = "% total",
			    age_crude.0= age_label[1],
			    age_crude.1= age_label[2],
			    age_crude.2= age_label[3],
			    crude="crude",
			    asr="ASR")

			  ft <- add_header(ft, ICCC_code = "ICCC3", ICCC_label = "ICCC3", 
			    CSU_C.0= "Number of cases",
			    CSU_C.1= "Number of cases",
			    CSU_C.2= "Number of cases",
			    total_cases = "Number of cases",
			    ratio = "Number of cases",
			    frequence = "Number of cases",
			    age_crude.0= "Rates per million",
			    age_crude.1= "Rates per million",
			    age_crude.2= "Rates per million",
			    crude="Rates per million",
			    asr="Rates per million",
			    top=TRUE)
			  
			  ft <- width(ft, j = 1, width = 0.5)
			  ft <- width(ft, j = 2, width = 1.7)
			  ft <- width(ft, j = 3:13, width = 0.5)
			  ft <- fontsize(ft, size = 10, part = "all")
			  ft <- merge_h(ft,i=1, part="header")
			  ft <- border(ft, i=c(1,nrow(dt_report)),border.top=fp_border(width = 1), part="body")
			  ft <- border(ft, i=nrow(dt_report),border.bottom=fp_border(width = 1), part="body")
			  ft <- border(ft, j=c(1,3,9),border.left=fp_border(width = 1), part="all")
			  ft <- border(ft, j=13,border.right=fp_border(width = 1), part="all")
			  ft <- align(ft, align="center", part="header")
			  ft <- height(ft, height = 0.1, part="header")
			  ft <- bg(ft, i = seq(1,nrow(dt_report),2), bg="#deebf7", part = "body")
			  ft <- bg(ft, i = nrow(dt_report), bg="#c6dbef", part = "body")
			  ft <- bg(ft, i = 1, bg="#c6dbef", part = "header")
			  ft <- bg(ft, i = 2, bg="#c6dbef", part = "header")

			  values$doc <- ph_with(values$doc, ft, location = ph_location(left=0.551, top=1.291))
				
			}
			 
			
			values$nb_slide <- values$nb_slide + 1
		
		})
	})
  
  #Download presentation
  output$downloadPres <- downloadHandler(
    filename = function() {
      paste0(input$pptx_filename, ".", "pptx")
      
    },
    content = function(file) {
      invalidateLater(100)
      values$nb_slide <- 0
      print(isolate(values$doc), file) 
     
    })
  
	#Download log
  output$downloadLog <- downloadHandler(
	
		filename =  paste0(gsub("\\W","", ls_args$label),"_",ls_args$sc,"_",gsub("\\D","", Sys.time()),"_error_log.txt"),
		content = function(file) {
			temp <- paste0(gsub("\\W","", ls_args$label),"_",ls_args$sc,"_",gsub("\\D","", Sys.time()),"_error_log.txt")
			canreg_shiny_error_log(file,temp)
		}
	
	)

  output$downloadShinyData <- downloadHandler(
	
		filename =  paste0(gsub("\\W","", ls_args$label),"_",ls_args$sc,"_",gsub("\\D","", Sys.time()),"_data.zip"),
		content = function(file) {

			withProgress(message = 'export shiny data', value = 0, {

				canreg_shiny_export_data(paste0(tempdir(),"/shinydata.txt"))
				incProgress(1/6, detail = "zip file")
				zip::zipr(file, c(paste0(tempdir(),"/shinydata.txt")))
				incProgress(1/6, detail = "")

			})
		}
	
	)
	
	#Download data
  output$downloadData <- downloadHandler(
	
		filename =  paste0(ls_args$sc,"_",gsub("\\D","", Sys.time()),"_data.csv"),
		content = function(file) {
			canreg_shiny_dwn_data(file)
		}
	
	)
  #Download report
  output$downloadReport <- downloadHandler(

		filename =  paste0(ls_args$sc,"_",gsub("\\D","", Sys.time()),"_report.docx"),
		content = function(file) {

			withProgress(message = 'Download report', value = 0, {

				canreg_shiny_dwn_report(file, download_dir, input$checkAnnexe)

			})
		}
	
	)
  #Download slide
  output$downloadSlide <- downloadHandler(

		filename =  paste0(ls_args$sc,"_",gsub("\\D","", Sys.time()),"_slide.pptx"),
		content = function(file) {

			withProgress(message = 'Download presentation', value = 0, {

				canreg_shiny_dwn_slide(file, input$checkAnnexe)
			})
		}
	
	)

  
  onevent("dblclick","directorypath", canreg_shiny_update_dwn_folder(output,values))


  observeEvent(input$downloadFile2,{ 

   	withProgress(message = 'Download output', value = 0, {



			path <- download_dir
			file_temp <- paste0(path, "/", input$text_filename)
			canreg_shiny_plot(dt_all(),  input,session, TRUE,FALSE,file_temp)
			incProgress(1, detail = "")


		})


  })

  observeEvent(input$CheckAdvanced,{

  	if (input$CheckAdvanced) {
  		show(id="advanced_menu", anim=TRUE)
  	}
  	else {
  		hide(id="advanced_menu", anim=TRUE)
  	}



  })

	observeEvent(input$shinydata,{ 

		withProgress(message = 'import shiny data', value = 0, {

				temp <- canreg_import_shiny_data(input$shinydata$datapath)
				if(!is.null(temp)) {
					ls_args <<- temp$ls_args
					dt_pyramid <<- temp$dt_pyramid
					dt_base <<- temp$dt_base
					dt_basis <<- temp$dt_basis
					dt_iccc <<- temp$dt_iccc
					canreg_age_group <<- canreg_get_agegroup_label(dt_base, ls_args$agegroup)
					year_info <<- canreg_get_years(dt_base)
					dt_CI5_label <<- as.character(unique(dt_CI5_list[cr == ls_args$sr, c("country_label"), with=FALSE])$country_label)
					i18n$set_translation_language(ls_args$lang)
					updateSelectInput(session, "select_table", selected = 1)
					updateSelectInput(session, "select_table", selected = 4)
					output$UI_regtitle <- renderText({ls_args$header})
					cancer_list <-  unique(dt_base$cancer_label)
					cancer_list <- sort(as.character(cancer_list))
					updateSelectInput(session,"selectCancerSite", choices=cancer_list)
					showNotification("Data imported",type="message")
				}
				else {
					incProgress(1, detail = "")
					showNotification("This is not a valid shiny data file",type="error")

				}
	 	 })

	})
		
})
