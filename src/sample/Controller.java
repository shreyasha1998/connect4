package sample;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscsPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField playerOneTextField,playerTwoTextField;
	@FXML
	public Button setNamesButton;

	private static final int COLUMNS=7;
	private static final int ROWS=6;
	private static final int CIRCLE_DIAMETER=80;
	private static final String discColor1="#24303E";
	private static final String disColor2="#4CAA88";
	private static String player1="Player One";
	private static String player2="Player Two";
	private boolean isPlayer1Turn=true;
	private final Disc[][]insertedDiscArray=new Disc[ROWS][COLUMNS];
	private boolean isAllowedToEnter=true;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	public void createPlayground()
	{
		Shape rectangleWithHoles=createGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList=createClickableColumns();
		for (Rectangle rectangle:rectangleList
		) {
			rootGridPane.add(rectangle,0,1);

		}
		setNamesButton.setOnAction(event -> {
			player1=playerOneTextField.getText();
			player2=playerTwoTextField.getText();
			playerNameLabel.setText(player1);
		});
	}

	private List<Rectangle> createClickableColumns() {
		List<Rectangle> rectangleList=new ArrayList<>();
		for(int cols=0;cols<COLUMNS;cols++)
		{
			Rectangle rectangle=new Rectangle(CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX((cols * (CIRCLE_DIAMETER + 5)) + CIRCLE_DIAMETER / 4);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column=cols;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowedToEnter) {
					isAllowedToEnter=false;
					insertDiscs(new Disc(isPlayer1Turn), column);
				}
			});

			rectangleList.add(rectangle);
		}
		return rectangleList;
	}

	private void insertDiscs(Disc disc, int column) {
		int rows=ROWS-1;
		while (rows>=0){
			if(getIfDiskPresent(rows,column)==null)
				break;
			rows--;
		}
		if(rows<0)
			return;
		insertedDiscArray[rows][column]=disc;
		insertedDiscsPane.getChildren().add(disc);
		disc.setTranslateX(column*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
		int currentRow=rows;
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(rows*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
		translateTransition.setOnFinished(event -> {
			isAllowedToEnter=true;
			if(gameEnded(currentRow,column)){
				gameOver();
			}
			isPlayer1Turn=!isPlayer1Turn;
			playerNameLabel.setText(isPlayer1Turn?player1:player2);
		});
		translateTransition.play();
	}

	private Disc getIfDiskPresent(int rows, int column) {
		if (rows>=ROWS||rows<0||column>=COLUMNS||column<0)
			return null;
		return insertedDiscArray[rows][column];
	}

	private void gameOver() {
		String winner=isPlayer1Turn?player1:player2;
		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The winner is "+winner);
		alert.setContentText("Do you want to play again?");
		ButtonType yesButton=new ButtonType("Yes");
		ButtonType noButton=new ButtonType("No,Exit");
		alert.getButtonTypes().setAll(yesButton,noButton);
		Platform.runLater(()->{
			Optional<ButtonType> buttonClicked=alert.showAndWait();
			if(buttonClicked.isPresent() && buttonClicked.get()==yesButton)
				resetGame();
			else {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {
		insertedDiscsPane.getChildren().clear();
		for (Disc[] discs : insertedDiscArray) {
			Arrays.fill(discs, null);
		}
		isPlayer1Turn=true;
		playerOneTextField.clear();
		playerTwoTextField.clear();
		player1="Player One";
		player2="Player Two";
		playerNameLabel.setText(player1);
		createPlayground();
	}

	private boolean gameEnded(int row, int column) {

		List<Point2D> verticalPoints= IntStream.rangeClosed(row-3,row+3)
				.mapToObj(r->new Point2D(r,column))
				.collect(Collectors.toList());
		List<Point2D> horizontalPoints= IntStream.rangeClosed(column-3,column+3)
				.mapToObj(col->new Point2D(row,col))
				.collect(Collectors.toList());
		Point2D startPoint1=new Point2D(row-3,column+3);
		List<Point2D> diagonal1Points=IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint1.add(i,-i))
				.collect(Collectors.toList());
		Point2D startPoint2=new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points=IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded=checkCombinations(verticalPoints)||checkCombinations(horizontalPoints)
				||checkCombinations(diagonal1Points)||checkCombinations(diagonal2Points);
		return isEnded;

	}

	private boolean checkCombinations(List<Point2D> Points) {
		int chain=0;
		for (Point2D point:Points) {
			int rowIndex=(int)point.getX();
			int columnIndex=(int)point.getY();
			Disc disc=getIfDiskPresent(rowIndex,columnIndex);
			if (disc!=null && disc.isPlayerOneMove==isPlayer1Turn) {
				chain++;

				if (chain == 4)
					return true;
			}
			else
				chain=0;
		}
		return false;
	}

	private Shape createGrid() {
		Shape rectangleWithHoles = new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);

		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS ; j++) {
				Circle circle=new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);
				circle.setTranslateX(j*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
				circle.setTranslateY(i*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
				rectangleWithHoles=Shape.subtract(rectangleWithHoles,circle);

			}

		}
		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}
	private static class Disc extends Circle{
		private final boolean isPlayerOneMove;
		public Disc(boolean isPlayerOneMove){
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setCenterX(CIRCLE_DIAMETER /2);
			setCenterY(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove?Color.valueOf(discColor1):Color.valueOf(disColor2));

		}
	}
}
