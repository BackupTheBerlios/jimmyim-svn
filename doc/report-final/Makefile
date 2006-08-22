FILE=report

$(FILE).pdf: $(FILE).tex $(FILE).aux $(FILE).toc uvod.tex razredi.tex ui.tex jabber.tex msn.tex icq.tex zakljucek.tex viri.tex
	pdflatex $(FILE).tex
	pdflatex $(FILE).tex

$(FILE).aux: $(FILE).tex
	pdflatex $(FILE).tex

$(FILE).toc: $(FILE).aux
	pdflatex $(FILE).tex

clean:
	rm -f *.pdf *.toc *.log *.aux *.lof *.lot *.aux *.blg *.dvi *~