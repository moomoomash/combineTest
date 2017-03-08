package com.example.truedemon.combinetest;

import java.math.BigInteger;
import java.util.Scanner;

public class MessageDecoder {
    static int[] MD1 = new int[300];
    static int[] MD2 = new int[300];
    static int[] MD3 = new int[300];
    static int[] coord = new int[2];
    static String MD1ToPrint = "";
    static String MD2toPrint = "";

    static int[][] matrixMD3 = new int [20][15];
    public MessageDecoder(){};

	/* NOTE:::::::::
	 *
	 *  THESE FUNCTIONS ARE PROVIDED FOR YOUR USAGE, THIS IS NOT TO BE USED AS AN ACTUAL CLASS
	 *  This are just the gists, you still have to put the functions at the correct places.
	 *
	 *
	 *
	 */

    //this function triggers if the length of message == 77, and the first two characters are a1
    //and third is not f
    //at the place you put it, check if SECOND CHARACTER is 1 and THIRD CHARACTER is not f
    //i.e if(receivedMessage.length() == 77 && receivedMessage.charAt(1) == '1')
    public static int[] getMapDescriptor1(String receivedMessage)
    {
        String newString = receivedMessage.substring(2);
        String binString = new BigInteger(newString, 16).toString(2);
        System.out.println(binString.length());
        if(binString.length() != 300)
        {
            String temp = "";
            int padAmt = 300 - binString.length();
            for(int i = 0; i < padAmt; i++)
            {
                temp = temp + "0";
            }
            binString = temp + binString;
        }
        System.out.println(binString.length());
        for(int i = 0; i < binString.length(); i++)
        {
            MD1[i] = Integer.parseInt(String.valueOf(binString.charAt(i)));
            System.out.print(MD1[i]);
        }

        return MD1;

    }

    //this function triggers if the first two characters are a2 and length is 77
    //and third is not f
    //at the place you put it, check if SECOND CHARACTER is 2 and THIRD CHARACTER is not f
    //i.e if(receivedMessage.length() == 77 && receivedMessage.charAt(1) == '2')

    public static int[] getMapDescriptor2(String receivedMessage)
    {
        String newString = receivedMessage.substring(2);
        String binString = new BigInteger(newString, 16).toString(2);
        System.out.println(binString.length());
        if(binString.length() != 300)
        {
            String temp = "";
            int padAmt = 300 - binString.length();
            for(int i = 0; i < padAmt; i++)
            {
                temp = temp + "0";
            }
            binString = temp + binString;
        }
        System.out.println(binString.length());
        for(int i = 0; i < binString.length(); i++)
        {
            MD2[i] = Integer.parseInt(String.valueOf(binString.charAt(i)));
            System.out.print(MD2[i]);
        }
        if(binString.length() != 300)
        {
            String temp = "";
            int padAmt = 300 - binString.length();
            for(int i = 0; i < padAmt; i++)
            {
                temp = temp + "0";
            }
            binString = temp + binString;
        }

        return MD2;

    }

    //this function adds the two mapDescriptors together, end product will be a 300 length int array,
    //where 0 = UNEXPLORED, 1 = EXPLORED, 2 = EXPLORED AND BLOCKED
    //and int[0] corresponds to [0][0], int[1] to [0][1] and so on, where 0,0 IS THE BOTTOM LEFT
    //use this info to decode and make the map when necessary
    public static int[] getMapDescriptor3()
    {
        for(int i = 0; i < MD2.length; i++)
        {
            MD3[i] = MD1[i] + MD2[i];
            System.out.print(MD3[i]);
        }


        return MD3;
    }

    //this function triggers if the message's SECOND CHARACTER is r
    //the format is "arXX,XX", where XX,XX is verticalcoord,horizontalcoord
    //i.e if(receivedMessage.charAt(1) == 'r')
    //coord[0] will be the VERTICAL COORDINATES, coord[1] will be the HORIZONTAL COORDINATES
    public static int[] getCoords(String receivedMessage)
    {
        String newString = receivedMessage.substring(2);
        String[] coordinates = newString.split(",");
        coord[0] = Integer.parseInt(coordinates[0]);
        coord[1] = Integer.parseInt(coordinates[1]);

        System.out.print(coord[0] + " " + coord[1]);
        return coord;
    }

    //this function retrieves MAP DESCRIPTOR 1 string REQUIRED AT THE END OF THE EXPLORATION and NEEDS TO BE PRINTED ON THE SCREEN
    //this is VERY IMPORTANT AS IT IS A SPECIAL PADDED STRING AND THIS WILL CONTRIBUTE TO THE SCORE OF THE EXPLORATION PHASE
    //it will trigger if the length is SECOND CHAR is '1' AND THE THIRD IS 'f'
    //i.e if(receivedMessage.charAt(1) == '1' && receivedMessage.charAt(2) == 'f' && receivedMessage.length() == 78)
    public static String getMD1toPrint(String receivedMessage)
    {
        String newString = receivedMessage.substring(3);
        System.out.print(newString);
        return newString;
    }

    //this function retrieves MAP DESCRIPTOR 2 string REQUIRED AT THE END OF THE EXPLORATION and NEEDS TO BE PRINTED ON THE SCREEN
    //this is VERY IMPORTANT AS IT IS A SPECIAL PADDED STRING AND THIS WILL CONTRIBUTE TO THE SCORE OF THE EXPLORATION PHASE
    //it will trigger if the length is SECOND CHAR is '2' AND THE THIRD IS 'f'
    //i.e if(receivedMessage.charAt(1) == '2' && receivedMessage.charAt(2) == 'f' && receivedMessage.length() == 78)
    public static String getMD2toPrint(String receivedMessage)
    {
        String newString = receivedMessage.substring(3);
        System.out.print(newString);
        return newString;
    }

    public void onClean()
    {
        MD1 = null;
        MD2 = null;
        MD3 = null;
        coord = null;
        MD1ToPrint = "";
        MD2toPrint = "";
    }
    public int [] getCoordinates(){
        return coord;
    }

    public static int [][] getMD3Matrix()
    {
        int [][] matrix = new int[20][15];
        int i , j, k=0;
        for (i = 19; i>=0;i--)
        {
            for ( j =0; j<15; j++)
            {
                matrix[i][j] = MD3[k];
                k++;
            }
        }
        matrixMD3 = matrix;
        return matrixMD3;
    }
    /*
     *
     *
     * For testing and how the if cases should look like
     *
     *
     *
     */
//    public static void main(String[] args)
//    {
//        Scanner scanner = new Scanner(System.in);
//        String receivedMessage = "";
//
//        while(true)
//        {
//            System.out.print("Test String : ");
//            receivedMessage = scanner.nextLine();
//            if(receivedMessage.length() == 77 && receivedMessage.charAt(1) == '1')
//            {
//                System.out.println("Map Desc 1 :");
//                getMapDescriptor1(receivedMessage);
//            }
//            else if(receivedMessage.length() == 77 && receivedMessage.charAt(1) == '2')
//            {
//                System.out.println("Map Desc 2 :");
//                getMapDescriptor2(receivedMessage);
//            }
//            else if(receivedMessage.charAt(1) == 'r')
//            {
//                getCoords(receivedMessage);
//            }
//            else if(receivedMessage.charAt(1) == '1' && receivedMessage.charAt(2) == 'f' && receivedMessage.length() == 78)
//            {
//                System.out.println("Map Desc 1 to print :");
//                getMD1toPrint(receivedMessage);
//            }
//            else if(receivedMessage.charAt(1) == '2' && receivedMessage.charAt(2) == 'f' && receivedMessage.length() == 78)
//            {
//                System.out.println("Map Desc 2 to print :");
//                getMD2toPrint(receivedMessage);
//            }
//            System.out.println("");
//        }
//
//
//    }

}
