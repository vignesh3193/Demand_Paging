import java.util.*;
import java.io.*;

public class Paging {

	//----------------------------------------------reading inputs and calling paging function-----------------------------------------------------------
	public static void main(String[] args)throws IOException 
	{		
		int machine_size=Integer.parseInt(args[0]);
		int page_size=Integer.parseInt(args[1]);
		int process_size=Integer.parseInt(args[2]);
		int J=Integer.parseInt(args[3]);
		int reference_count=Integer.parseInt(args[4]);
		int q=3;
		String replacement_algo=args[5];
		
		File random_numbers=new File("random-numbers.txt");
		Scanner sc=new Scanner(random_numbers);
		
		demand_paging(sc, machine_size, page_size, process_size, J, reference_count, replacement_algo, q);
		sc.close();
	}
	
	//-----------------------------------------------Main function(performs the demand paging)----------------------------------------
	public static void demand_paging(Scanner sc, int machine_size, int page_size, int process_size, int J, int reference_count, String replacement_algo, int q)
	{
		int ram=machine_size/page_size;
		int frame_table[]=new int[ram];
		ArrayList<Integer> FIFO=new ArrayList<Integer>();
		ArrayList<Integer> LRU=new ArrayList<Integer>();
		ArrayList<Integer> completed=new ArrayList<Integer>();
		double temp[]=set_parameters(J);
		int N=(int)temp[0];
		double A=temp[1];
		double B=temp[2];
		double C=temp[3];		
		int current_reference[]=new int[N];
		int reference_number[]=new int[N];
		int active_process=0;
		int active_reference=0;
		
		//----------------------------initialization loop-------------------------------------------------------------
		for(int i=0;i<ram;i++)
		{
			frame_table[i]=-1;			
		}
		for(int i=0;i<N;i++)
		{
			current_reference[i]=mod(111*(i+1),process_size);
			reference_number[i]=0;
		}
		
			
			int i=0;
		while(completed.size()<N)
		{
			int req_page=-1;
			for(int j=0;j<q;j++)
			{
				active_reference=current_reference[active_process];
				req_page=active_reference/10;
				req_page=req_page*10+active_process;
				int flag=0;
				
				for(int k=0;k<ram;k++)
				{
					if(frame_table[k]==req_page)
					{
						System.out.println((active_process+1)+"  requests "+active_reference+" (Page "+(req_page-active_process)/10+") at time "+(i+1)+" hit in frame "+ k);
						flag=1;
						
						if(LRU.contains((Object)req_page))
						{
							LRU.remove((Object)req_page);
							LRU.add(req_page);
						}
					}
				}
					
				if(flag==0)
				{		
					//int rep_page=page_replacement(FIFO,LRU,replacement_algo);
					System.out.println((active_process+1)+"  requests "+active_reference+" (Page "+req_page+") at time "+(i+1)+" fault, using frame ");
					if(!FIFO.isEmpty())
					{
						FIFO.remove(0);	
						LRU.remove(0);
					}
					
					FIFO.add(req_page);
					LRU.add(req_page);
				}
				
				double next_reference=getReference(A,B,C,sc,active_reference, process_size); 
				current_reference[active_process]=(int)next_reference;
				reference_number[active_process]++;
				
				if(replacement_algo.equals("fifo"))
				{
					for(int k=0;k<FIFO.size();k++)
					{
						frame_table[k]=FIFO.get(k);
					}
				}
				else if(replacement_algo.equals("lru"))
				{
					for(int k=0;k<FIFO.size();k++)
					{
						frame_table[k]=LRU.get(k);
					}
				}
				
				i++;
				if(reference_number[active_process]==reference_count)
				{
					completed.add(active_process);
					break;
				}
			}
			if(active_process==N-1)
			{
				active_process=0;
			}
			else
			{
				active_process++;
			}
			
		}
		
	}
	
	
	//----------------------------------------using random numbers to get reference-----------------------------------------
	public static double getReference(double A, double B, double C, Scanner sc, int reference, int S)
	{	
		int r=sc.nextInt();
		//System.out.println("uses random "+r);
		double d=1-(A+B+C);
		double y=r/(Integer.MAX_VALUE+1d);
		
		if(y<A)
		{
			return mod(reference+1,S);
		}
		else if(y<A+B)
		{
			return mod(reference-5,S);
		}
		else if(y<A+B+C)
		{
			return mod(reference+4,S);
		}
		else
		{
			return mod(sc.nextInt(),S);
		}
	}
	
	//----------------------------------------Setting probability parameters (A,B,C)-----------------------------------------
	public static double[] set_parameters(int J)
	{
		double temp[]=new double[4];
		
		if(J==1)
		{
			temp[0]=1;
			temp[1]=1;
			temp[2]=0;
			temp[3]=0;
			
		}
		else if(J==2)
		{
			temp[0]=4;
			temp[1]=1;
			temp[2]=0;
			temp[3]=0;			
		}
		else if(J==3)
		{
			temp[0]=4;
			temp[1]=0;
			temp[2]=0;
			temp[3]=0;
		}
		else
		{
			temp[0]=1;
			temp[1]=0.75;
			temp[2]=0.25;
			temp[3]=0;
		}
		
		return temp;
	}
	
	
	//-----------------------------------------Page replacement function-----------------------------------------------
	public static int page_replacement(ArrayList<Integer> FIFO, ArrayList<Integer> LRU, String algo)
	{
		return 0;
	}
	
	//------------------------------------------CUSTOM MOD FUNCTION---------------------------------------------------
	public static int mod(int a, int b)
	{
		return (a+b)%b;
	}
	
		
}
