USE [RestaurantDB]
GO
/****** Object:  Table [dbo].[Bookings]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Bookings](
	[booking_id] [int] IDENTITY(1,1) NOT NULL,
	[customer_name] [nvarchar](100) NOT NULL,
	[phone] [varchar](20) NOT NULL,
	[table_id] [int] NOT NULL,
	[booking_time] [datetime] NOT NULL,
	[created_at] [datetime] NULL,
	[status] [varchar](20) NULL,
PRIMARY KEY CLUSTERED
(
	[booking_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Foods]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Foods](
	[food_id] [int] IDENTITY(1,1) NOT NULL,
	[food_name] [nvarchar](100) NOT NULL,
	[price] [decimal](18, 2) NOT NULL,
	[category] [nvarchar](50) NULL,
	[status] [varchar](20) NULL,
	[image_path] [nvarchar](500) NULL,
	[is_active] [bit] NULL,
PRIMARY KEY CLUSTERED
(
	[food_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OrderDetails]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OrderDetails](
	[detail_id] [int] IDENTITY(1,1) NOT NULL,
	[order_id] [int] NOT NULL,
	[food_id] [int] NOT NULL,
	[quantity] [int] NULL,
	[price_at_order] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[detail_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Orders]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Orders](
	[order_id] [int] IDENTITY(1,1) NOT NULL,
	[table_id] [int] NULL,
	[user_id] [int] NOT NULL,
	[order_date] [datetime] NULL,
	[total_amount] [decimal](18, 2) NULL,
	[status] [varchar](20) NULL,
PRIMARY KEY CLUSTERED
(
	[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Shifts]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Shifts](
	[shift_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[shift_date] [date] NOT NULL,
	[shift_name] [nvarchar](50) NULL,
	[note] [nvarchar](200) NULL,
PRIMARY KEY CLUSTERED
(
	[shift_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Tables]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Tables](
	[table_id] [int] IDENTITY(1,1) NOT NULL,
	[table_name] [nvarchar](50) NOT NULL,
	[capacity] [int] NULL,
	[status] [varchar](20) NULL,
	[floor] [int] NULL,
PRIMARY KEY CLUSTERED
(
	[table_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Tools]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Tools](
	[tool_id] [int] IDENTITY(1,1) NOT NULL,
	[tool_name] [nvarchar](100) NOT NULL,
	[quantity] [int] NULL,
	[min_threshold] [int] NULL,
	[status] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED
(
	[tool_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Users]    Script Date: 08/12/2025 2:25:04 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[password] [varchar](255) NOT NULL,
	[full_name] [nvarchar](100) NOT NULL,
	[role] [varchar](20) NOT NULL,
	[avatar_path] [nvarchar](500) NULL,
	[phone] [varchar](15) NULL,
	[created_at] [datetime] NULL,
	[is_active] [bit] NULL,
PRIMARY KEY CLUSTERED
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Bookings] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Bookings] ADD  DEFAULT ('CONFIRMED') FOR [status]
GO
ALTER TABLE [dbo].[Foods] ADD  DEFAULT (N'Món chính') FOR [category]
GO
ALTER TABLE [dbo].[Foods] ADD  DEFAULT ('AVAILABLE') FOR [status]
GO
ALTER TABLE [dbo].[Foods] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[OrderDetails] ADD  DEFAULT ((1)) FOR [quantity]
GO
ALTER TABLE [dbo].[Orders] ADD  DEFAULT (getdate()) FOR [order_date]
GO
ALTER TABLE [dbo].[Orders] ADD  DEFAULT ((0)) FOR [total_amount]
GO
ALTER TABLE [dbo].[Orders] ADD  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[Tables] ADD  DEFAULT ((4)) FOR [capacity]
GO
ALTER TABLE [dbo].[Tables] ADD  DEFAULT ('EMPTY') FOR [status]
GO
ALTER TABLE [dbo].[Tables] ADD  DEFAULT ((1)) FOR [floor]
GO
ALTER TABLE [dbo].[Tools] ADD  DEFAULT ((0)) FOR [quantity]
GO
ALTER TABLE [dbo].[Tools] ADD  DEFAULT ((10)) FOR [min_threshold]
GO
ALTER TABLE [dbo].[Tools] ADD  DEFAULT (N'Tốt') FOR [status]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD FOREIGN KEY([table_id])
REFERENCES [dbo].[Tables] ([table_id])
GO
ALTER TABLE [dbo].[OrderDetails]  WITH CHECK ADD FOREIGN KEY([food_id])
REFERENCES [dbo].[Foods] ([food_id])
GO
ALTER TABLE [dbo].[OrderDetails]  WITH CHECK ADD FOREIGN KEY([order_id])
REFERENCES [dbo].[Orders] ([order_id])
GO
ALTER TABLE [dbo].[Orders]  WITH CHECK ADD FOREIGN KEY([table_id])
REFERENCES [dbo].[Tables] ([table_id])
GO
ALTER TABLE [dbo].[Orders]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Shifts]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Users]  WITH CHECK ADD CHECK  (([role]='STAFF' OR [role]='MANAGER'))
GO
