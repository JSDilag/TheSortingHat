package sorting_hat.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import mini_game.Viewport;
import sorting_hat.TheSortingHat.SortingHatPropertyType;
import sorting_hat.data.SortingHatLevelRecord;
import sorting_hat.data.SortingHatDataModel;
import sorting_hat.data.SortingHatRecord;
import sorting_hat.ui.SortingHatMiniGame;
import properties_manager.PropertiesManager;
import static sorting_hat.SortingHatConstants.*;
import sorting_hat.data.SnakeCell;
import sorting_hat.data.SortingHatAlgorithm;
import sorting_hat.data.SortingHatAlgorithmFactory;
import sorting_hat.data.SortingHatAlgorithmType;

/**
 * This class provides services for efficiently loading and saving
 * binary files for The Sorting Hat game application.
 * 
 * @author Richard McKenna & ___________________
 */
public class SortingHatFileManager
{
    // WE'LL LET THE GAME KNOW WHEN DATA LOADING IS COMPLETE
    private SortingHatMiniGame miniGame;
    
    /**
     * Constructor for initializing this file manager, it simply keeps
     * the game for later.
     * 
     * @param initMiniGame The game for which this class loads data.
     */
    public SortingHatFileManager(SortingHatMiniGame initMiniGame)
    {
        // KEEP IT FOR LATER
        miniGame = initMiniGame;
    }

    /**
     * This method loads the contents of the levelFile argument so that
     * the player may then play that level. 
     * 
     * @param levelFile Level to load.
     */
    public void loadLevel(String levelFile)
    {
        // LOAD THE RAW DATA SO WE CAN USE IT
        // OUR LEVEL FILES WILL HAVE THE DIMENSIONS FIRST,
        // FOLLOWED BY THE GRID VALUES
        try
        {
            File fileToOpen = new File(levelFile);

            // LET'S USE A FAST LOADING TECHNIQUE. WE'LL LOAD ALL OF THE
            // BYTES AT ONCE INTO A BYTE ARRAY, AND THEN PICK THAT APART.
            // THIS IS FAST BECAUSE IT ONLY HAS TO DO FILE READING ONCE
            byte[] bytes = new byte[Long.valueOf(fileToOpen.length()).intValue()];
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            FileInputStream fis = new FileInputStream(fileToOpen);
            BufferedInputStream bis = new BufferedInputStream(fis);
            
            // HERE IT IS, THE ONLY READY REQUEST WE NEED
            bis.read(bytes);
            bis.close();
            
            // NOW WE NEED TO LOAD THE DATA FROM THE BYTE ARRAY
            DataInputStream dis = new DataInputStream(bais);
            
            // NOTE THAT WE NEED TO LOAD THE DATA IN THE SAME
            // ORDER AND FORMAT AS WE SAVED IT
            
            // FIRST READ THE ALGORITHM NAME TO USE FOR THE LEVEL
            String algorithmName = dis.readUTF();
            SortingHatAlgorithmType algorithmTypeToUse = SortingHatAlgorithmType.valueOf(algorithmName);
            SortingHatAlgorithm algorithmToUse = SortingHatAlgorithmFactory.buildSortingHatAlgorithm(algorithmTypeToUse, ((SortingHatDataModel)miniGame.getDataModel()).getTilesToSort());
            
            // THEN READ THE GRID DIMENSIONS
            // WE DON'T ACTUALLY USE THESE
            int initGridColumns = dis.readInt();
            int initGridRows = dis.readInt();
            
            ArrayList<SnakeCell> newSnake = new ArrayList();
            
            // READ IN THE SNAKE CELLS, KEEPING TRACK OF THE
            // GRID BOUNDS AS WE GO
            int initSnakeLength = dis.readInt();
            int minCol = initSnakeLength; int maxCol = 0; int minRow = initSnakeLength; int maxRow = 0;
            for (int i = 0; i < initSnakeLength; i++)
            {
                int col = dis.readInt();
                int row = dis.readInt();
                if (col < minCol) minCol = col;
                if (row < minRow) minRow = row;
                if (col > maxCol) maxCol = col;
                if (row > maxRow) maxRow = row;
                SnakeCell newCell = new SnakeCell(col, row);
                newSnake.add(newCell);
            }
            int numColumns = maxCol - minCol + 1;
            int numRows = maxRow - minRow + 1;
            
            // WE SHOULD NOW HAVE THE CORRECT MIN AND MAX ROWS AND COLUMNS,
            // SO LET'S USE THAT INFO TO CORRECT THE SNAKE SO THAT IT'S PACKED
            for (int i = 0; i < newSnake.size(); i++)
            {
                SnakeCell sC = newSnake.get(i);
                sC.col -= minCol;
                sC.row -= minRow;
            }
            
            // EVERYTHING WENT AS PLANNED SO LET'S MAKE IT PERMANENT
            SortingHatDataModel dataModel = (SortingHatDataModel)miniGame.getDataModel();
            Viewport viewport = dataModel.getViewport();
            viewport.setGameWorldSize(numColumns * TILE_WIDTH, numRows * TILE_HEIGHT);
            viewport.setNorthPanelHeight(NORTH_PANEL_HEIGHT);
            viewport.initViewportMargins();
            dataModel.setCurrentLevel(levelFile);
            dataModel.initLevel(levelFile, newSnake, algorithmToUse);
        }
        catch(Exception e)
        {
            // LEVEL LOADING ERROR
            miniGame.getErrorHandler().processError(SortingHatPropertyType.TEXT_ERROR_LOADING_LEVEL);
        }
    }    
    
    /**
     * This method saves the record argument to the player records file.
     * 
     * @param record The complete player record, which has the records
     * on all levels.
     */
    public void saveRecord(SortingHatRecord record)
    {
        SortingHatRecord recordToSave = record;
        
        
        try{
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            String recordPath = PATH_DATA + props.getProperty(SortingHatPropertyType.FILE_PLAYER_RECORD);
            
            File fileToClose = new File(recordPath);
            FileOutputStream fos = new FileOutputStream(fileToClose);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
           
            byte[] recordInBytes = recordToSave.toByteArray();
            
            
            bos.write(recordInBytes); 
            DataOutputStream dos = new DataOutputStream(bos);
            bos.close();
            
        }catch(Exception e){
            
        }
    }

    /**
     * This method loads the player record from the records file
     * so that the user may view stats.
     * 
     * @return The fully loaded record from the player record file.
     */
    public SortingHatRecord loadRecord()
    {
        SortingHatRecord recordToLoad = new SortingHatRecord();
        
        // LOAD THE RAW DATA SO WE CAN USE IT
        // OUR LEVEL FILES WILL HAVE THE DIMENSIONS FIRST,
        // FOLLOWED BY THE GRID VALUES
        try
        {
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            String recordPath = PATH_DATA + props.getProperty(SortingHatPropertyType.FILE_PLAYER_RECORD);
            File fileToOpen = new File(recordPath);

            // LET'S USE A FAST LOADING TECHNIQUE. WE'LL LOAD ALL OF THE
            // BYTES AT ONCE INTO A BYTE ARRAY, AND THEN PICK THAT APART.
            // THIS IS FAST BECAUSE IT ONLY HAS TO DO FILE READING ONCE
            byte[] bytes = new byte[Long.valueOf(fileToOpen.length()).intValue()];
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            FileInputStream fis = new FileInputStream(fileToOpen);
            BufferedInputStream bis = new BufferedInputStream(fis);
            
            // HERE IT IS, THE ONLY READY REQUEST WE NEED
            bis.read(bytes);
            bis.close();
            
            // NOW WE NEED TO LOAD THE DATA FROM THE BYTE ARRAY
            DataInputStream dis = new DataInputStream(bais);
            
            // NOTE THAT WE NEED TO LOAD THE DATA IN THE SAME
            // ORDER AND FORMAT AS WE SAVED IT
            // FIRST READ THE NUMBER OF LEVELS
            int numLevels = dis.readInt();

            for (int i = 0; i < numLevels; i++)
            {
                String levelName = dis.readUTF();
                SortingHatLevelRecord rec = new SortingHatLevelRecord();
                rec.algorithm = dis.readUTF();
                rec.gamesPlayed = dis.readInt();
                rec.wins = dis.readInt();
                rec.perfectWins = dis.readInt();
                rec.fastestPerfectWin = dis.readLong();
                recordToLoad.addSortingHatLevelRecord(levelName, rec);
            }
        }
        catch(Exception e)
        {
            // THERE WAS NO RECORD TO LOAD, SO WE'LL JUST RETURN AN
            // EMPTY ONE AND SQUELCH THIS EXCEPTION
        }        
        return recordToLoad;
    }
}