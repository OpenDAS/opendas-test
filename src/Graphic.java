import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Properties;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.function.Function2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.*;
import org.jfree.data.statistics.Regression;

public class Graphic extends JFrame{	
	static JFrame F = new JFrame();private 
	static final long serialVersionUID = 1L;	
	static JPanel PanS = new JPanel();
	int i;		
	double Y = 0;
	static double ValReg,ValYMaxReg,ValXMaxReg,ValXMinReg,ValYMinReg;
	static double ValMax=0,ValMin,ValMoy=0,Val;
	static double sumY = 0.0,sumX = 0.0;
	
	ValueMarker markerMax = new ValueMarker(ValMax);ValueMarker markerMin = new ValueMarker(ValMin);ValueMarker markerMoy = new ValueMarker(ValMoy);
	XYTextAnnotation AnnotationMax;XYTextAnnotation AnnotationMin;XYTextAnnotation AnnotationMoy;XYTextAnnotation AnnotationReg;
	DecimalFormat DF =new DecimalFormat("0.00");
	DecimalFormat DF2 =new DecimalFormat("0.000000");
	Properties properties=new Properties();
	/**
	 * Créer un graphique à l'aide des tableaux d'ordonnées et d'abssisses.
	 * @param TabX tableau contenant le temps global de l'application.
	 * @param TabY tableau contenant le temps de réponse du serveur pour chaques trames envoyées.
	 * @param Points le nombre de points dans chaques tableaux. 
	 */
	public Graphic (String applicationTitle, String chartTitle,long[]charX,double[]charY, int Points,boolean Fin) {		 
		
		//Création de la collection, en passant les tableaux de valeurs en paramètre.
        XYSeriesCollection dataset = createDataset(charX,charY,Points);        	
		// Sur la base du dataset on peut créer le graphe.
		JFreeChart chart = createChart(dataset, chartTitle,Points,Fin);        	
        //On intègre le graphique dans la fenêtre.
        ChartPanel chartPanel= new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(F.getSize().width,F.getSize().height-40));        
        //On enlève le graphique précédent.
        PanS.removeAll();        	
        //On place le nouveau graphique.
        PanS.add(chartPanel); 
        
        F.setVisible(true);	       
	    F.setLocation(50,200);
	    F.setSize(910, 620);			
		F.setTitle(applicationTitle);      
		F.getContentPane().add(PanS, BorderLayout.SOUTH);
    }
	/**
	 * Créer un dataset avec les paramètres reçus.
	 * @param charX
	 * @param charY
	 * @param Points
	 * @return data L'ensemble des données utiles pour créer le graphique.
	 */
	private XYSeriesCollection createDataset(long[] charX,double[]charY,int Points){
		
		//Créer une nouvelle série donc ligne.
		final XYSeries series1 = new XYSeries("Scénario");			
		//Définis les points du graphe.			
		for(i=0;i<=Points;i++)
		{				
			Y=charY[i];
			if(Y!=0) series1.add(charX[i], charY[i]);			
		}		
        //Synthétise les séries en un graphe.
        final XYSeriesCollection XYSeries = new XYSeriesCollection(); 
        XYSeries.addSeries(series1);        
		return XYSeries;		
	}
	/**
	 * Permet la mise en forme du graphique, la création de la valeur moyenne, des markeurs Min et Max et des annotations.
	 * @param data
	 * @param title
	 * @param Points
	 * @param Fin
	 * @return chart, le graphique complet.
	 */
	private JFreeChart createChart(XYSeriesCollection data, String title,int Points,boolean Fin) {
		
        //Création du graphique.
		final JFreeChart JChart = ChartFactory.createXYLineChart(title,"Durée d'exécution (ms)", "Temps de Réponse (ms)",data, 
		PlotOrientation.VERTICAL,true,false,false);
		
		//Création des plots.
		XYPlot plot = (XYPlot) JChart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.black);
		plot.setRangeGridlinePaint(Color.black);
		
		//Uniquement pour le graphique final.
		if(Fin==true){			
			ValMin=(Double)data.getSeries(0).getY(0);
		    XYItemRenderer RenduDispersion = plot.getRenderer();		    
		    StandardXYItemRenderer RenduRegression = new StandardXYItemRenderer();
		    XYSplineRenderer PolyRenderer = new XYSplineRenderer(2);
		    RenduRegression.setBaseSeriesVisibleInLegend(false);
		    Font Police= new Font("Tahoma", Font.BOLD,10);
		    for(int T=0;T<Points;T++){
		    	//Calcul de la somme des points.
		    	ValMoy=ValMoy+((Double)data.getSeries(0).getY(T));	
		    }
		    //Parcourt l'ensemble des points contenus dans le tableau.
		    for(int M=0;M<Points;M++){		    		
			    Val=(Double)data.getSeries(0).getY(M);
			    if(Val>=ValMax){	    	
			    	ValMax=Val;			    	
			    }			    
			    if(Val<=ValMin){	    	
			    	ValMin=Val;			    	
			    }			    
		    }
		    //Calcul et affichage des différents markeurs.
		    markerMoy.setValue(ValMoy/((double)Points));
		    markerMax.setValue(ValMax);
		    markerMin.setValue(ValMin);
		    
		    //Modification du nombre de chiffre après la virgule et de la couleur de la courbe.		    	    
		    markerMoy.setPaint(Color.ORANGE);
		    markerMax.setPaint(Color.GREEN);
		    markerMin.setPaint(Color.CYAN);
		    
		    //Modification de la largeur de la ligne.
	    	markerMoy.setStroke(new BasicStroke(2));
	    	markerMax.setStroke(new BasicStroke(2));
	    	markerMin.setStroke(new BasicStroke(2));
	    	
	    	/*Ajout de l'annotation aux coordonnées indiqués, on divise la somme des valeurs par	    	 
	    	le nombre de points+1 car l'index du graphique débute a zéro et pas à un, le nombre de cycle n'aurait pas été complet.*/ 	    	
	    	AnnotationMax = new XYTextAnnotation("Max="+DF.format(ValMax)+" ms", (Double)data.getSeries(0).getX(Points/2), ValMax);
	    	AnnotationMax.setFont(Police);
	    	AnnotationMin = new XYTextAnnotation("Min="+DF.format(ValMin)+" ms", (Double)data.getSeries(0).getX(Points/2), ValMin);
	    	AnnotationMin.setFont(Police);	    	
	    	
	    	//Ajout des nouveaux marqueurs en ordonnée.
	    	plot.addRangeMarker(markerMoy);
	    	plot.addRangeMarker(markerMax);
	    	plot.addRangeMarker(markerMin);
		    plot.addAnnotation(AnnotationMax);plot.addAnnotation(AnnotationMin);		    
		    
		    //Création de la courbe de régression polynomial.		    
		    double[] result = Regression.getPolynomialRegression(data, 0, 3);
		    double[] params = new double[result.length - 1];
			System.arraycopy(result, 0, params, 0, result.length -1);
			PolynomialFunction2D function = new PolynomialFunction2D(params);
			XYDataset regression = DatasetUtilities.sampleFunction2D(function, 0, (Double)data.getSeries(0).getX(Points-1), 20, "Regression Polynomiale");
			plot.setDataset(1, regression);    
			plot.setRenderer(1, PolyRenderer);
					
			//Création de la courbe de régression linéaire.
			plot.setDataset(2, Regression(data));
		    plot.setRenderer(2,RenduRegression);	  
		    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		    DrawingSupplier DS = plot.getDrawingSupplier();			    
		    
		    //Ajout des nouveaux paramètres dans la légende
		    LegendItemCollection colorLegends =new LegendItemCollection();			
			colorLegends.add(new LegendItem("Scénario1",Color.red)); 
			colorLegends.add(new LegendItem("Régression Polynomiale",Color.blue)); 
			colorLegends.add(new LegendItem("Régression Linéaire Y= "+(DF2.format(ValReg/(ValXMaxReg-ValXMinReg))+"X"+"+"+DF.format(ValYMinReg)),Color.black)); 
			colorLegends.add(new LegendItem("Moyenne = "+DF.format(ValMoy/(double)Points)+" ms",Color.yellow)); 			
			plot.setFixedLegendItems(colorLegends);
			
			// Pour chaque itération on ajoute la courbe associé.
		    for(int i =0;i< data.getSeriesCount();i++){
		    	Paint paint =DS.getNextPaint();	        	        
		        RenduDispersion.setSeriesPaint(i, paint);	
		        RenduRegression.setSeriesPaint(i,Color.black);			        
		        RenduRegression.setSeriesStroke(i,new BasicStroke(2));
		        PolyRenderer.setSeriesStroke(i, new BasicStroke(2));
		        AnnotationReg = new XYTextAnnotation("Diff="+DF.format(ValReg),ValXMaxReg,ValYMaxReg+1);
		        AnnotationReg.setFont(new Font("Tahoma", Font.BOLD,10));
		        plot.addAnnotation(AnnotationReg);
		    }
		    ValMoy=0;
		    ValMax=0;
		    ValMin=0;		    
		                  
		    try {
		    	properties.load(new FileInputStream("config.properties"));
		    	String GraphFile=properties.getProperty("GraphKey");
		    	File XYlineChart=new File(GraphFile);
		    	ChartUtilities.saveChartAsPNG(XYlineChart,JChart,1024,680);
		    } catch (IOException e) {			
		    	System.out.println("Impossible de sauvegarder le graphique");
		    } 
		}		
        return JChart;
    }
	/**
	 * Lit le dataset contenant les points et créer la courbe de régression points par points.
	 * @param data
	 * @return coll: un autre dataset.
	 */
	private static XYDataset Regression(XYSeriesCollection data){
	
		XYSeriesCollection Regressioncollection = new XYSeriesCollection();
		double xMin = Double.MAX_VALUE, xMax = 0;			
		//On détermine au préalable les valeurs max et min.
		for(int i=0;i<1; i++){
			XYSeries ser =data.getSeries(i);
			for(int j =0;j<ser.getItemCount();j++){
				double x =ser.getX(j).doubleValue();
				if(x < xMin) xMin = x;
				if(x > xMax) xMax = x;
			}
		}		
		for(int i=0;i<1;i++){
			XYSeries ser = data.getSeries(i);
			int n = ser.getItemCount();			
			double sx = 0, sy = 0, sxx = 0, sxy = 0;
			for (int j=0; j<n; j++){
				//On choisit les valeurs des abcisses et des ordonnées.
				double x = ser.getX(j).doubleValue();
				double y = ser.getY(j).doubleValue();
				//Somme des valeurs en abssices et en ordonnées.
				sx = sx+x;
				sy = sy+y;
				//Somme des carrés des valeurs en abssice et en ordonné.
				sxx = sxx+x*x;
				sxy = sxy+x*y;				
			}			
			double a = ((n*sxy)-(sx*sy))/((n*sxx)-(sx*sx));
			double b = sy/n - a*sx/n;
			XYSeries regr = new XYSeries(ser.getKey());
			regr.add(xMin,b+a*xMin);
			regr.add(xMax,b+a*xMax);
			Regressioncollection.addSeries(regr);
			ValReg=((Double)Regressioncollection.getSeries(0).getY(1)-(Double)Regressioncollection.getSeries(0).getY(0));
			ValXMaxReg=(Double)Regressioncollection.getSeries(0).getX(1);
			ValXMinReg=(Double)Regressioncollection.getSeries(0).getX(0);
			ValYMaxReg=(Double)Regressioncollection.getSeries(0).getY(1);
			ValYMinReg=(Double)Regressioncollection.getSeries(0).getY(0);
		}		
		return Regressioncollection;
	}
	public static double[] getPolynomialRegression(XYSeriesCollection dataset, int series, int order) {
		
		int itemCount = dataset.getItemCount(series);		
		int validItems = 0;
		double[][] data = new double[2][itemCount];
		for(int item = 0; item < itemCount; item++){
			double x = dataset.getXValue(series, item);
		    double y = dataset.getYValue(series, item);
		    if (!Double.isNaN(x) && !Double.isNaN(y)){
		    	data[0][validItems] = x;
		        data[1][validItems] = y;
		        validItems++;
		    }
		}		
		int equations = order + 1;
		int coefficients = order + 2;
		double[] result = new double[equations + 1];
		double[][] matrix = new double[equations][coefficients];	
		
		for(int item = 0; item < validItems; item++){
			sumX += data[0][item];
		    sumY += data[1][item];
		    for(int eq = 0; eq < equations; eq++){
		    	for(int coe = 0; coe < coefficients - 1; coe++){
		    		matrix[eq][coe] += Math.pow(data[0][item],eq + coe);
		         }
		         matrix[eq][coefficients - 1] += data[1][item]
		                        * Math.pow(data[0][item],eq);
		         }
		    }
		    double[][] subMatrix = calculateSubMatrix(matrix);
		    for (int eq = 1; eq < equations; eq++) {
		    	matrix[eq][0] = 0;
		        for (int coe = 1; coe < coefficients; coe++) {
		        	matrix[eq][coe] = subMatrix[eq - 1][coe - 1];
		        }
		    }
		    for (int eq = equations - 1; eq > -1; eq--) {
		    	double value = matrix[eq][coefficients - 1];
		        for (int coe = eq; coe < coefficients -1; coe++) {
		        	value -= matrix[eq][coe] * result[coe];
		        }
		        result[eq] = value / matrix[eq][eq];
		    }
		    double meanY = sumY / validItems;
		    double yObsSquare = 0.0;
		    double yRegSquare = 0.0;
		    for (int item = 0; item < validItems; item++) {
		    	double yCalc = 0;
		        for (int eq = 0; eq < equations; eq++) {
		        	yCalc += result[eq] * Math.pow(data[0][item],eq);
		        }
		        yRegSquare += Math.pow(yCalc - meanY, 2);
		        yObsSquare += Math.pow(data[1][item] - meanY, 2);
		    }
		    double rSquare = yRegSquare / yObsSquare;
		    result[equations] = rSquare;		    
		    System.out.println(result[0]+","+result[1]+","+result[2]+","+result[2]+";"+validItems+","+itemCount+",");
		    
		    return result;
	}
	
	
	private static double[][] calculateSubMatrix(double[][] matrix){
		int equations = matrix.length;
		int coefficients = matrix[0].length;
		double[][] result = new double[equations - 1][coefficients - 1];
		for (int eq = 1; eq < equations; eq++) {
			double factor = matrix[0][0] / matrix[eq][0];
		    for (int coe = 1; coe < coefficients; coe++) {
		    	result[eq - 1][coe -1] = matrix[0][coe] - matrix[eq][coe]
		                       * factor;
		    }
		}
		if (equations == 1) {
			return result;
		}
		// check for zero pivot element
		if (result[0][0] == 0) {
		boolean found = false;
			for (int i = 0; i < result.length; i ++) {
				if (result[i][0] != 0) {
					found = true;
			        double[] temp = result[0];
			        for (int j = 0; j < result[i].length; j++) {
			        	result[0][j] = result[i][j];
			        }
			        for (int j = 0; j < temp.length; j++) {
			        	result[i][j] = temp[j];
			        }
			        break;
			    }
			}
			if (!found) {
				System.out.println("Equation has no solution!");
			    return new double[equations - 1][coefficients - 1];
			}
		}
		double[][] subMatrix = calculateSubMatrix(result);
		for (int eq = 1; eq < equations -  1; eq++) {
			result[eq][0] = 0;
		    for (int coe = 1; coe < coefficients - 1; coe++) {
		    	result[eq][coe] = subMatrix[eq - 1][coe - 1];
		    }
		}
		return result;
	}		
}
class PolynomialFunction2D implements Function2D {    
	double[] coefficients;   
    int order;
    
    public PolynomialFunction2D(double[] coefficients) {
        this.coefficients = coefficients;
        this.order = coefficients.length - 1;
    }
    public double getValue(double x) {
        double y = 0;
        for(int i = 0; i < coefficients.length; i++){
            y += coefficients[i] * Math.pow(x, i);
        }
        return y;
    }
}