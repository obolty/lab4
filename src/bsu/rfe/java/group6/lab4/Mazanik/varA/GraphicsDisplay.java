package bsu.rfe.java.group6.lab4.Mazanik.varA;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

import javax.swing.JPanel;


@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
// Список координат точек для построения графика
	private Double[][] graphicsData;
// Флаговые переменные, задающие правила отображения графика
	private boolean showAxis = true;
	private boolean showMarkers = true;
// Границы диапазона пространства, подлежащего отображению
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	// Используемый масштаб отображения
	private double scale;
// Используемый масштаб отображения
	private double scaleX;
	private double scaleY;
// Различные стили черчения линий
	private BasicStroke graphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
// Различные шрифты отображения надписей
	private Font axisFont;
	private Font signFont;
	private double FontMetrics;
	private Point2D.Double startDragPoint;
	private Rectangle2D.Double selectionRect;
	private double originalScale; // Для сохранения исходного масштаба
	private double originalMinX, originalMaxX, originalMinY, originalMaxY;

	private boolean leftButtonPressed = false;

	public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
		setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
		graphicsStroke = new BasicStroke(6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				new float[] { 3,3 }, 0.0f);
// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
		axisFont = new Font("Serif", Font.BOLD, 36);
		signFont = new Font("Serif", Font.PLAIN, 10);

	}

	protected double pointToX(double x) {
		return minX + x / scaleX;
	}

	protected double pointToY(double y) {
		return maxY - y / scaleY;
	}

	protected void paintGrid(Graphics2D canvas) {
		canvas.setStroke(new BasicStroke(0.05f));
		canvas.setColor(Color.LIGHT_GRAY);
		canvas.setFont(signFont);
		canvas.setColor(Color.LIGHT_GRAY);

		// Вертикальные линии
		double x = minX;
		while (x <= maxX) {
			Point2D.Double start = xyToPoint(x, minY);
			Point2D.Double end = xyToPoint(x, maxY);
			canvas.draw(new Line2D.Double(start, end));

			// Рисуем координаты только на оси X (y = 0)
			if (minY <= 0 && 0 <= maxY) { // Проверяем, пересекает ли ось X область отображения
				Point2D.Double axisPoint = xyToPoint(x, 0);
				canvas.setColor(Color.BLACK);
				String xCoord = String.format("%.2f", x);
				FontMetrics fm = canvas.getFontMetrics();
				double textWidth = fm.stringWidth(xCoord);
				double textHeight = fm.getHeight();
				if(showAxis)
				canvas.drawString(xCoord, (int) (axisPoint.x - textWidth / 2), (int) (axisPoint.y + textHeight + 5));
				canvas.setColor(Color.LIGHT_GRAY);
			}

			x += (maxX - minX) / 40; // Изменено на 40 для большей детализации
		}

		// Горизонтальные линии
		double y = minY;
		while (y <= maxY) {
			Point2D.Double start = xyToPoint(minX, y);
			Point2D.Double end = xyToPoint(maxX, y);
			canvas.draw(new Line2D.Double(start, end));

			// Рисуем координаты только на оси Y (x = 0)
			if (minX <= 0 && 0 <= maxX) { // Проверяем, пересекает ли ось Y область отображения
				Point2D.Double axisPoint = xyToPoint(0, y);
				canvas.setColor(Color.BLACK);
				String yCoord = String.format("%.2f", y);
				FontMetrics fm = canvas.getFontMetrics();
				double textWidth = fm.stringWidth(yCoord);
				double textHeight = fm.getHeight();
				if(showAxis)
				canvas.drawString(yCoord, (int) (axisPoint.x - textWidth - 5), (int) (axisPoint.y + textHeight / 4));
				canvas.setColor(Color.LIGHT_GRAY);
			}

			y += (maxY - minY) / 20;
		}
	}

// Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
		this.graphicsData = graphicsData;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
		repaint();
	}

// Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}

	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
		repaint();
	}

// Метод отображения всего компонента, содержащего график
	public void paintComponent(Graphics g) {
		/*
		 * Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона Эта
		 * функциональность - единственное, что осталось в наследство от paintComponent
		 * класса JPanel
		 */
		super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
		if (graphicsData == null || graphicsData.length == 0)
			return;
// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
// Это необходимо для определения области пространства, подлежащей отображению
// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
		minX = graphicsData[0][0];
		maxX = graphicsData[graphicsData.length - 1][0];
		minY = graphicsData[0][1];
		maxY = minY;
// Найти минимальное и максимальное значение функции
		for (int i = 1; i < graphicsData.length; i++) {
			if (graphicsData[i][1] < minY) {
				minY = graphicsData[i][1];
			}
			if (graphicsData[i][1] > maxY) {
				maxY = graphicsData[i][1];
			}
		}
		/*
		 * Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y - сколько
		 * пикселов приходится на единицу длины по X и по Y
		 */
		double scaleX = getSize().getWidth() / (maxX - minX);
		double scaleY = getSize().getHeight() / (maxY - minY);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
// Выбираем за основу минимальный
		scale = Math.min(scaleX, scaleY);
//Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
		if (scale == scaleX) {
			/*
			 * Если за основу был взят масштаб по оси X, значит по оси Y делений меньше,
			 * т.е. подлежащий визуализации диапазон по Y будет меньше высоты окна. Значит
			 * необходимо добавить делений, сделаем это так: 1) Вычислим, сколько делений
			 * влезет по Y при выбранном масштабе - getSize().getHeight()/scale 2) Вычтем из
			 * этого сколько делений требовалось изначально 3) Набросим по половине
			 * недостающего расстояния на maxY и minY
			 */
			double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
			maxY += yIncrement; // Добавляем небольшой отступ
			minY -= yIncrement;
		}
		if (scale == scaleY) {
//Если за основу был взят масштаб по оси Y, действовать по аналогии
			double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
			maxX += xIncrement;
			minX -= xIncrement;
		}
		// double scalingFactor = 0.5; // Adjust this value (between 0 and 1) to control
		// the
		// graph size
		// scale *= scalingFactor;
//Шаг 7 - Сохранить текущие настройки холста
		Graphics2D canvas = (Graphics2D) g;
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		Font oldFont = canvas.getFont();
//Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
//Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
//Первыми (если нужно) отрисовываются оси координат.
		if (showAxis)
			paintAxis(canvas);
		paintGrid(canvas);
//Затем отображается сам график
		paintGraphics(canvas);
//Затем (если нужно) отображаются маркеры точек, по которым строился график.
		if (showMarkers)
			paintMarkers(canvas);
//Шаг 9 - Восстановить старые настройки холста
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);

		if (selectionRect != null) {
			canvas.setColor(Color.RED);
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
			canvas.setStroke(dashed);
			canvas.draw(selectionRect);
		}

		originalMinX = minX; // Используйте исходные значения minX, maxX, minY, maxY
		originalMaxX = maxX;
		originalMinY = minY;
		originalMaxY = maxY;
		originalScale = scale;

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				System.out.println("Mouse pressed: " + e.getX() + ", " + e.getY() + ", button: " + e.getButton());
				if (e.getButton() == MouseEvent.BUTTON1) { // Левая кнопка - начало выделения
					startDragPoint = new Point2D.Double(e.getX(), e.getY());
					selectionRect = new Rectangle2D.Double(); // Создаем прямоугольник выделения
					originalScale = scale; // Сохраняем исходный масштаб
					leftButtonPressed = true;
				} else if (e.getButton() == MouseEvent.BUTTON3) { // Правая кнопка - сброс масштаба
					scale = originalScale; // Восстанавливаем исходный масштаб
					selectionRect = null; // Убираем прямоугольник выделения
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && selectionRect != null) { // Завершение выделения
					// 1. Пересчитываем координаты выделенной области в координаты графика
                    double x1 = pointToX(selectionRect.getMinX());
                    double y1 = pointToY(selectionRect.getMaxY());
                    double x2 = pointToX(selectionRect.getMaxX());
                    double y2 = pointToY(selectionRect.getMinY());

                    // 2. Вычисляем центр выделенной области
                    double centerX = (x1 + x2) / 2;
                    double centerY = (y1 + y2) / 2;

                    // 3. Вычисляем новый масштаб
                    double newScaleX = getWidth() / (x2 - x1);
                    double newScaleY = getHeight() / (y1 - y2);
                    scale = Math.min(newScaleX, newScaleY);

                    // 4. Обновляем границы отображения с учетом центрирования
                    minX = centerX - getWidth() / (2 * scale);
                    maxX = centerX + getWidth() / (2 * scale);
                    minY = centerY - getHeight() / (2 * scale);
                    maxY = centerY + getHeight() / (2 * scale);


                    selectionRect = null;
                    repaint();
                    
                } else if (e.getButton() == MouseEvent.BUTTON3) { // Правая кнопка - сброс масштаба
                    scale = originalScale;
                    minX = originalMinX;
                    maxX = originalMaxX;
                    minY = originalMinY;
                    maxY = originalMaxY;
                    selectionRect = null;
                    repaint();
                }
                leftButtonPressed = false;
            }
        });

		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// Подсказка с координатами точки
				if (graphicsData != null) {
					for (Double[] point : graphicsData) {
						Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
						double distance = screenPoint.distance(e.getX(), e.getY());
						if (distance < 10) { // Если курсор близко к маркеpу
							setToolTipText("X = " + point[0] + ",Y = " + point[1] + "");
							return; // Выходим из цикла, чтобы не отображать несколько подсказок
						}
					}
				}
				setToolTipText(null); // Убираем подсказку, если курсор не над маркером
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (leftButtonPressed && startDragPoint != null) { // Используем флаг
					selectionRect.setRect(Math.min(startDragPoint.x, e.getX()), Math.min(startDragPoint.y, e.getY()),
							Math.abs(startDragPoint.x - e.getX()), Math.abs(startDragPoint.y - e.getY()));
					repaint();
				}
			}
		});
	}

//Отрисовка графика по прочитанным координатам
	protected void paintGraphics(Graphics2D canvas) {
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//Выбрать линию для рисования графика
		canvas.setStroke(graphicsStroke);
//Выбрать цвет линии
		canvas.setColor(Color.RED);
		/*
		 * Будем рисовать линию графика как путь, состоящий из множества сегментов
		 * (GeneralPath) Начало пути устанавливается в первую точку графика, после чего
		 * прямой соединяется со следующими точками
		 */
		GeneralPath graphics = new GeneralPath();
		if (graphicsData != null) { // Проверка на null
			for (int i = 0; i < graphicsData.length; i++) {
				Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
				if (i == 0) {
					graphics.moveTo(point.getX(), point.getY()); // Начало пути в первой точке
				} else {
					graphics.lineTo(point.getX(), point.getY()); // Линия к последующим точкам
				}
			}
		}
		canvas.draw(graphics);
	}

	// Метод для рисования маркеров
	protected void paintMarkers(Graphics2D canvas) {
		canvas.setStroke(markerStroke);

		for (Double[] point : graphicsData) {
			int sum = sumDigits(point[1].intValue());
			Color markerColor = (sum < 10) ? Color.BLUE : Color.RED;
			canvas.setColor(markerColor);
			canvas.setPaint(markerColor);

			Point2D.Double center = xyToPoint(point[0], point[1]);

			// Создаем равносторонний треугольник
			GeneralPath triangle = new GeneralPath();
			double side = 11; // Размер стороны треугольника
			double height = side * Math.sqrt(3) / 2;

			triangle.moveTo(center.x, center.y + height / 3); // Верхняя вершина
			triangle.lineTo(center.x - side / 2, center.y - 2 * height / 3); // Левая вершина
			triangle.lineTo(center.x + side / 2, center.y - 2 * height / 3); // Правая вершина
			triangle.closePath();

			canvas.draw(triangle); // Рисуем контур треугольника
			canvas.fill(triangle); // Заполняем треугольник цветом
		}
	}

// Метод, обеспечивающий отображение осей координат
	protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
		canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
		canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
		canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
		canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
		FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
		if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
// а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
			canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
// Стрелка оси Y
			GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
			arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
			arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
			Rectangle2D bounds = axisFont.getStringBounds("y", context);
			Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
		}
// Определить, должна ли быть видна ось X на графике
		if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
			canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
// Стрелка оси X
			GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
			arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
			arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
			Rectangle2D bounds = axisFont.getStringBounds("x", context);
			Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10),
					(float) (labelPos.getY() + bounds.getY()));
		}
	}

	/*
	 * Метод-помощник, осуществляющий преобразование координат. Оно необходимо, т.к.
	 * верхнему левому углу холста с координатами (0.0, 0.0) соответствует точка
	 * графика с координатами (minX, maxY), где minX - это самое "левое" значение X,
	 * а maxY - самое "верхнее" значение Y.
	 */
	protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
		double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
		double deltaY = maxY - y;
		return new Point2D.Double(deltaX * scale, deltaY * scale);
	}

	/*
	 * Метод-помощник, возвращающий экземпляр класса Point2D.Double смещѐнный по
	 * отношению к исходному на deltaX, deltaY К сожалению, стандартного метода,
	 * выполняющего такую задачу, нет.
	 */
	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
// Инициализировать новый экземпляр точки
		Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}

	private int sumDigits(int n) {
		int sum = 0;
		n = Math.abs(n); // Берем модуль числа, чтобы работать с положительными значениями
		while (n > 0) {
			sum += n % 10;
			n /= 10;
		}
		return sum;
	}

}