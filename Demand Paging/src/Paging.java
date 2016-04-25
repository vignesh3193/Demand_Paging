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

		System.out.println("The machine size is "+machine_size);
		System.out.println("The page size is "+page_size);
		System.out.println("The process size is "+process_size);
		System.out.println("The job mix number is "+J);
		System.out.println("The number of references per process is "+reference_count);
		System.out.println("The replacement algorithm is "+replacement_algo);
		System.out.println();


		demand_paging(sc, machine_size, page_size, process_size, J, reference_count, replacement_algo, q);
		sc.close();
	}

	//-----------------------------------------------Main function(performs the demand paging)----------------------------------------
	public static void demand_paging(Scanner sc, int machine_size, int page_size, int process_size, int J, int reference_count, String replacement_algo, int q)
	{
		int ram=machine_size/page_size;
		int frame_pages=0;
		int frame_table[]=new int[ram];
		ArrayList<Integer> FIFO=new ArrayList<Integer>();
		ArrayList<Integer> LRU=new ArrayList<Integer>();
		ArrayList<Integer> RANDOM=new ArrayList<Integer>();
		ArrayList<Integer> completed=new ArrayList<Integer>();
		double temp[]=set_parameters(J);
		int N=(int)temp[0];
		double A=temp[1];
		double B=temp[2];
		double C=temp[3];		
		int current_reference[]=new int[N];
		int reference_number[]=new int[N];
		HashMap<Integer,Integer> entry_time=new HashMap<Integer,Integer>();
		int residency[]=new int[N];
		int evictions[]=new int[N];
		int faults[]=new int[N];
		int active_process=0;
		int active_reference=0;
		int total_residency=0;
		int total_evictions=0;

		//----------------------------initialization loop-------------------------------------------------------------
		for(int i=0;i<ram;i++)
		{
			frame_table[i]=-1;			
		}
		for(int i=0;i<N;i++)
		{
			residency[i]=0;
			evictions[i]=0;
			faults[i]=0;
			current_reference[i]=mod(111*(i+1),process_size);
			reference_number[i]=0;
			faults[i]=0;
		}


		int i=0;
		while(completed.size()<N)
		{
			int req_page=-1;
			for(int j=0;j<q;j++)
			{ 

				active_reference=current_reference[active_process];
				req_page=active_reference/page_size;
				req_page=req_page*10+active_process;
				//System.out.println(req_page);
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
					System.out.println((active_process+1)+"  requests "+active_reference+" (Page "+(req_page-active_process)/10+") at time "+(i+1)+" fault, using frame ");
					faults[active_process]++;
					int remove_index=0;
					if(frame_pages==ram)
					{
						int out_page=0;
						

						

						if(replacement_algo.equals("random") && RANDOM.size()>1)
						{
							int r=sc.nextInt();
							//System.out.println("uses random (page repl)"+r);
							remove_index=mod(r,RANDOM.size());						
							
							//System.out.println(RANDOM.toString());
						}
						
						if(replacement_algo.equals("fifo")||replacement_algo.equals("lru"))
						{
							out_page=FIFO.get(0);
							residency[out_page%10]+=(i-entry_time.get(FIFO.get(0)));
							total_residency+=(i-entry_time.get(FIFO.get(0)));
							evictions[out_page%10]++;
							total_evictions++;
						}
						else if(replacement_algo.equals("lru"))
						{
							out_page=LRU.get(0);
							residency[out_page%10]+=(i-entry_time.get(LRU.get(0)));
							total_residency+=(i-entry_time.get(LRU.get(0)));
							evictions[out_page%10]++;
							total_evictions++;
						}
						else if(replacement_algo.equals("random"))
						{
							out_page=RANDOM.get(remove_index);
							residency[out_page%10]+=(i-entry_time.get(out_page));
							total_residency+=(i-entry_time.get(RANDOM.get(remove_index)));
							evictions[out_page%10]++;
							total_evictions++;
							System.out.println("removing page "+out_page);
						}
						
						frame_pages--;
						FIFO.remove(0);	
						LRU.remove(0);
						RANDOM.remove(remove_index);


					}

					FIFO.add(req_page);
					LRU.add(req_page);
					RANDOM.add(remove_index,req_page);
					frame_pages++;

					entry_time.put(req_page, i);




				}

				if(J==4)
				{ 
					if(active_process==0)
					{
						A=0.75;
						B=0.25;
						C=0;
					}
					if(active_process==1)
					{
						A=0.75;
						B=0;
						C=0.25;
					}
					if(active_process==2)
					{
						A=0.75;
						B=0.125;
						C=0.125;
					}
					if(active_process==3)
					{
						A=0.5;
						B=0.125;
						C=0.125;
					}

				}

				double next_reference=getReference(A,B,C,sc,active_reference, process_size); 
				current_reference[active_process]=(int)next_reference;
				reference_number[active_process]++;

				if(replacement_algo.equals("fifo"))
				{
					for(int k=0;k<FIFO.size();k++)
					{
						frame_table[ram-k-1]=FIFO.get(k);
						//System.out.println("frame table");
						//System.out.println(frame_table[k]);
					}
				}
				else if(replacement_algo.equals("lru"))
				{
					for(int k=0;k<LRU.size();k++)
					{
						frame_table[ram-k-1]=LRU.get(k);
					}
				}
				else if(replacement_algo.equals("random"))
				{
					//System.out.println("frame table");
					for(int k=0;k<RANDOM.size();k++)
					{
						frame_table[ram-k-1]=RANDOM.get(k);

						//System.out.println(frame_table[ram-k-1]);
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
				//System.out.println("in here");
				active_process++;
			}

		}
		print_results(residency,evictions,total_residency,total_evictions,faults);
	}


	//----------------------------------------using random numbers to get reference-----------------------------------------
	public static double getReference(double A, double B, double C, Scanner sc, int reference, int S)
	{	
		int r=sc.nextInt();
		//System.out.println("uses random (next ref)"+r);
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
			int r1=sc.nextInt();
			//System.out.println("uses random (D)"+r1);
			return mod(r1,S);
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
			temp[0]=4;
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

	//-------------------------------------------Print Results--------------------------------------------------------
	public static void print_results(int[] residency,int[] evictions,int total_residency,int total_evictions,int[] faults)
	{

		int total_faults=0;
		System.out.println();
		for(int i=0;i<evictions.length;i++)
		{
			System.out.println("Process "+(i+1)+" had "+ faults[i]+" faults and "+((double)residency[i]/evictions[i])+" avg residency");
			total_faults+=faults[i];
		}
		System.out.println();
		System.out.println("Total number of faults is "+total_faults+" and avg residency is "+((double)total_residency/total_evictions));
	}
}
