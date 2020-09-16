//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkit
//              (ACE extensions)

package cuny.blender.englishie.evaluation;

import java.util.*;
import java.io.*;


/**
 * assigns ACE events to a Document, given the entities, times, and values.
 */

public class VI {
	static final int M = 5;
	static final int K = 6;
	static final int N= 13;
	public static void main (String[] args) throws IOException {

		//double[][] num = new double[][]{{4,1,0},{0,2,4},{0,0,1},{0,0,1},{0,0,1}};
		//double[][] num = new double[][]{{4,1,0,0},{0,0,2,4},{0,0,0,1},{0,0,0,1},{0,0,0,1}};
		//double[][] num = new double[][]{{4,1,2,0},{0,0,0,4},{0,0,0,1},{0,0,0,1},{0,0,0,1}};
		//double[][] num = new double[][]{{4,3,0},{0,0,4},{0,0,1},{0,0,1},{0,0,1}};
		//double[][] num = new double[][]{{1,0},{1,0},{1,0},{1,0},{0,1},{0,4}};
		//double[][] num = new double[][]{{1,0},{1,0},{1,0},{1,0},{1,0},{0,4}};
		//double[][] num = new double[][]{{5,0,0,0,0,0,0,0,0},{0,1,1,0,0,0,0,0,0},{0,0,0,1,1,0,0,0,0},
		//		{0,0,0,0,0,1,1,0,0},{0,0,0,0,0,0,0,1,1}};
		/*double[][] num = new double[][]{{4,1,0,0,0,0},{0,0,2,0,0,0},{0,0,0,2,0,0},
				{0,0,0,0,2,0},{0,0,0,0,0,2}};
		double entropy_C = 0;
		for (int i=0;i<K;i++){
			double b = 0;
			for (int j=0;j<M;j++){
				b+= num[j][i];
			}
			entropy_C+= (b/N)*Math.log(b/N)/Math.log(2);
		}
		entropy_C=-entropy_C;
		System.out.println("entropy_C="+entropy_C);
		
		double entropy_R = 0;
		for (int i=0;i<M;i++){
			double b = 0;
			for (int j=0;j<K;j++){
				b+= num[i][j];
			}
			entropy_R+= (b/N)*Math.log(b/N)/Math.log(2);
		}
		entropy_R=-entropy_R;
		System.out.println("entropy_R="+entropy_R);
		
		double entropy_C_R = 0;
		for (int i=0;i<M;i++){
			double b = 0;
			for (int j=0;j<K;j++){
				b+= num[i][j];
			}
			for (int j=0;j<K;j++){
				if (num[i][j]==0)
					continue;
				entropy_C_R+= (num[i][j]/N)*Math.log(num[i][j]/b)/Math.log(2);
				
			}
		}
		entropy_C_R=-entropy_C_R;
		
		System.out.println("entropy_C_R="+entropy_C_R);
		
		double entropy_R_C = 0;
		for (int i=0;i<K;i++){
			double b = 0;
			for (int j=0;j<M;j++){
				b+= num[j][i];
			}
			for (int j=0;j<M;j++){
				if (num[j][i]==0)
					continue;
				entropy_R_C+= (num[j][i]/N)*Math.log(num[j][i]/b)/Math.log(2);
			}
		}
		entropy_R_C=-entropy_R_C;
		System.out.println("entropy_R_C="+entropy_R_C);
		System.out.println("entropy_C_R+entropy_R_C="+(entropy_C_R+entropy_R_C));
		double h=1-entropy_R_C/entropy_R;
		double c=1-entropy_C_R/entropy_C;
		double V=2*h*c/(h+c);
		System.out.println("V="+V);
		*/
		/*double p=(double)(5+(double)71/21)/14;
		double r=(double)(3+(double)101/15)/14;
		double f=2*p*r/(p+r);
		System.out.println("p="+p);
		System.out.println("r="+r);
		System.out.println("f="+f);*/
		
		double p=(double)1;
		double r=(double)7/8;
		double f=2*p*r/(p+r);
		System.out.println("p="+p);
		System.out.println("r="+r);
		System.out.println("f="+f);
		/*double a=13;
		double b=17;
		double c=12;
		double d=49;
		System.out.println(2*(a*d-b*c)/((a+b)*(d+b)+(a+c)*(d+c)));
		
		d=51;
		b=15;
		System.out.println(2*(a*d-b*c)/((a+b)*(d+b)+(a+c)*(d+c)));
		
		a=13; b=15; c=14; d=49;
		System.out.println(2*(a*d-b*c)/((a+b)*(d+b)+(a+c)*(d+c)));
		
		a=15;b=15;c=12;d=49;
		System.out.println(2*(a*d-b*c)/((a+b)*(d+b)+(a+c)*(d+c)));
		
		a=6;b=10;c=0;d=20;
		System.out.println(2*(a*d-b*c)/((a+b)*(d+b)+(a+c)*(d+c)));
		
		a=10;b=0;c=4;d=64;
		System.out.println(2*(a*d-b*c)/((a+b)*(d+b)+(a+c)*(d+c)));*/
		
	}

}
